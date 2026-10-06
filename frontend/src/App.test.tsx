import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import App from "./App";

vi.mock("./features/resources/ResourceTable", () => ({
  ResourceTable: () => <div>resource list</div>,
}));

describe("App", () => {
  it("renders the resource booking shell and list", () => {
    render(<App />);

    expect(screen.getByRole("heading", { name: "Ressourcen-Buchung" })).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Buchbare Ressourcen" })).toBeInTheDocument();
    expect(screen.getByText("resource list")).toBeInTheDocument();
  });
});
