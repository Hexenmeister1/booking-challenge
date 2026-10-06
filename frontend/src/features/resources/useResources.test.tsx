import { act, renderHook, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { useResources } from "./useResources";

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("useResources", () => {
  it("uses a fallback message when the request rejects with a non-Error", async () => {
    vi.stubGlobal("fetch", vi.fn().mockRejectedValue("network failure"));

    const { result } = renderHook(() => useResources());

    await waitFor(() =>
      expect(result.current.error).toBe("Die Ressourcen konnten nicht geladen werden."),
    );
  });

  it("ignores the request failure after the hook is unmounted", async () => {
    const fetch = vi.fn(
      (_input: RequestInfo | URL, init?: RequestInit) =>
        new Promise<Response>((_resolve, reject) => {
          init?.signal?.addEventListener("abort", () =>
            reject(new DOMException("Aborted", "AbortError")),
          );
        }),
    );
    vi.stubGlobal("fetch", fetch);

    const { unmount } = renderHook(() => useResources());
    await waitFor(() => expect(fetch).toHaveBeenCalledOnce());

    await act(async () => {
      unmount();
      await Promise.resolve();
    });

    expect(fetch.mock.calls[0][1]?.signal?.aborted).toBe(true);
  });
});
