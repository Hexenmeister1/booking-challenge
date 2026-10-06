package de.example.booking.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(name = "ApiProblem", description = "RFC 9457 Fehlerantwort der API.")
public record ApiProblem(
        @Schema(description = "Problemtyp", example = "about:blank") String type,
        @Schema(description = "Kurzer Fehlertitel", example = "Zeitraum nicht verfügbar")
                String title,
        @Schema(description = "HTTP-Statuscode", example = "409") Integer status,
        @Schema(description = "Verständliche Fehlerbeschreibung") String detail,
        @Schema(description = "Request-Pfad", example = "/api/bookings") String instance,
        @Schema(description = "Feldfehler bei ungültigen Eingaben") Map<String, String> fieldErrors) {}