import { afterEach, describe, expect, it, vi } from "vitest";
import { ApiError, apiGet, apiRequest } from "./client";

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("apiRequest", () => {
  it("sends GET requests and returns the JSON response", async () => {
    const response = { ok: true, status: 200, json: vi.fn().mockResolvedValue({ id: 1 }) };
    const fetch = vi.fn().mockResolvedValue(response);
    vi.stubGlobal("fetch", fetch);

    await expect(apiGet("/api/resources/1")).resolves.toEqual({ id: 1 });
    expect(fetch).toHaveBeenCalledWith("/api/resources/1", { method: "GET", headers: {} });
  });

  it("adds a JSON content type for a body and preserves caller headers", async () => {
    const fetch = vi.fn().mockResolvedValue({ ok: true, status: 201, json: () => "created" });
    vi.stubGlobal("fetch", fetch);

    await apiRequest("/api/bookings", {
      method: "POST",
      body: JSON.stringify({ name: "Ada" }),
      headers: { Authorization: "Bearer test" },
    });

    expect(fetch).toHaveBeenCalledWith("/api/bookings", {
      method: "POST",
      body: JSON.stringify({ name: "Ada" }),
      headers: { "Content-Type": "application/json", Authorization: "Bearer test" },
    });
  });

  it("returns undefined for a no-content response", async () => {
    const json = vi.fn();
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({ ok: true, status: 204, json }));

    await expect(apiRequest<void>("/api/bookings/1", { method: "DELETE" })).resolves.toBeUndefined();
    expect(json).not.toHaveBeenCalled();
  });

  it("uses the problem detail, title, then HTTP fallback for errors", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn()
        .mockResolvedValueOnce({
          ok: false,
          status: 409,
          statusText: "Conflict",
          json: () => Promise.resolve({ title: "Conflict", detail: "Already booked" }),
        })
        .mockResolvedValueOnce({
          ok: false,
          status: 400,
          statusText: "Bad Request",
          json: () => Promise.resolve({ title: "Invalid request" }),
        })
        .mockResolvedValueOnce({
          ok: false,
          status: 503,
          statusText: "Service Unavailable",
          json: () => Promise.resolve({}),
        })
        .mockResolvedValueOnce({
          ok: false,
          status: 502,
          statusText: "",
          json: () => Promise.reject(new Error("not JSON")),
        }),
    );
    await expect(apiGet("/first")).rejects.toMatchObject({
      name: "ApiError",
      status: 409,
      message: "Already booked",
    } satisfies Partial<ApiError>);
    await expect(apiGet("/second")).rejects.toMatchObject({ message: "Invalid request" });
    await expect(apiGet("/third")).rejects.toMatchObject({ message: "Service Unavailable" });
    await expect(apiGet("/fourth")).rejects.toMatchObject({ message: "HTTP 502" });
  });
});
