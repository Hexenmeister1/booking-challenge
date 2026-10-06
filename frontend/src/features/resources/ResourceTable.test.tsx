import { fireEvent, render, screen, within } from "@testing-library/react";
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

  it("zeigt den Konfliktgrund beim Versuch, eine belegte Ressource zu buchen", async () => {
    const fetch = vi
      .fn()
      .mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(resources),
      })
      .mockResolvedValueOnce({
        ok: false,
        status: 409,
        statusText: "Conflict",
        json: () =>
          Promise.resolve({
            title: "Zeitraum nicht verfügbar",
            detail: "Besprechungsraum Nord ist im gewählten Zeitraum bereits gebucht.",
          }),
      });
    vi.stubGlobal("fetch", fetch);
    const user = userEvent.setup();

    render(<ResourceTable />);
    await screen.findByText("Besprechungsraum Nord");
    await user.click(screen.getAllByRole("button", { name: "Buchen" })[0]);
    const dialog = await screen.findByRole("dialog");
    const form = within(dialog);
    fireEvent.change(await form.findByLabelText(/Gebucht von/), {
      target: { value: "Ada" },
    });
    fireEvent.change(await form.findByLabelText(/Beginn/), {
      target: { value: "2032-04-05T09:00" },
    });
    fireEvent.change(await form.findByLabelText(/Ende/), {
      target: { value: "2032-04-05T10:00" },
    });
    await user.click(form.getByRole("button", { name: "Buchen" }));

    expect(
      await screen.findByText(
        "Besprechungsraum Nord ist im gewählten Zeitraum bereits gebucht.",
      ),
    ).toBeInTheDocument();
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
