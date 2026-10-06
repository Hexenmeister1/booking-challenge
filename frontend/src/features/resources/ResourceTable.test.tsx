import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it, vi } from "vitest";
import { ResourceTable } from "./ResourceTable";

const resources = [
  {
    id: 1,
    name: "Besprechungsraum Nord",
    category: "ROOM",
    location: "Haus 1, 2. OG",
    capacity: 8,
  },
  {
    id: 2,
    name: "Ultraschallgerät",
    category: "DEVICE",
    location: "Haus 3, Labor",
    capacity: 1,
  },
];

function stubFetchWith(response: Partial<Response>) {
  vi.stubGlobal("fetch", vi.fn().mockResolvedValue(response));
}

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("ResourceTable", () => {
  it("zeigt einen Ladeindikator solange die Anfrage offen ist", () => {
    vi.stubGlobal("fetch", vi.fn(() => new Promise(() => {})));

    render(<ResourceTable />);

    expect(
      screen.getByRole("progressbar", { name: "Ressourcen werden geladen" }),
    ).toBeInTheDocument();
  });

  it("zeigt die vom Backend gelieferten Ressourcen", async () => {
    stubFetchWith({
      ok: true,
      status: 200,
      json: () => Promise.resolve(resources),
    });

    render(<ResourceTable />);

    expect(
      await screen.findByText("Besprechungsraum Nord"),
    ).toBeInTheDocument();
    expect(screen.getByText("Ultraschallgerät")).toBeInTheDocument();
    // One row per resource plus the header row.
    expect(screen.getAllByRole("row")).toHaveLength(resources.length + 1);
  });

  it("zeigt die Begründung des Backends statt einer leeren Tabelle", async () => {
    stubFetchWith({
      ok: false,
      status: 503,
      statusText: "Service Unavailable",
      json: () =>
        Promise.resolve({
          title: "Nicht erreichbar",
          detail: "Die Datenbank antwortet nicht.",
        }),
    });

    render(<ResourceTable />);

    expect(
      await screen.findByText("Die Datenbank antwortet nicht."),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: "Erneut versuchen" }),
    ).toBeInTheDocument();
  });

  it("lädt die Ressourcen nach einem fehlgeschlagenen Aufruf erneut", async () => {
    const fetch = vi
      .fn()
      .mockRejectedValueOnce(new Error("Verbindung fehlgeschlagen"))
      .mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(resources),
      });
    vi.stubGlobal("fetch", fetch);
    const user = userEvent.setup();

    render(<ResourceTable />);

    await screen.findByText("Verbindung fehlgeschlagen");
    await user.click(screen.getByRole("button", { name: "Erneut versuchen" }));

    expect(await screen.findByText("Besprechungsraum Nord")).toBeInTheDocument();
    expect(fetch).toHaveBeenCalledTimes(2);
  });
});
