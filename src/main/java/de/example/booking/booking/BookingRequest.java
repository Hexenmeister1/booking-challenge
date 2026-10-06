package de.example.booking.booking;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Angaben zum Anlegen einer Buchung.")
public record BookingRequest(
        @Schema(description = "ID der zu buchenden Ressource", example = "1")
                @NotNull @Positive Long resourceId,
        @Schema(description = "Name der buchenden Person", example = "Ada Lovelace")
                @NotBlank @Size(max = 120) String bookedBy,
        @Schema(description = "Lokaler Beginn inklusive Datum und Uhrzeit", example = "2032-04-05T09:00:00")
                @NotNull LocalDateTime startsAt,
        @Schema(description = "Lokales Ende inklusive Datum und Uhrzeit", example = "2032-04-05T10:00:00")
                @NotNull LocalDateTime endsAt) {}
