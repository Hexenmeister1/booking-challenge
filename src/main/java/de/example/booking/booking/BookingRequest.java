package de.example.booking.booking;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record BookingRequest(
        @NotNull @Positive Long resourceId,
        @NotBlank @Size(max = 120) String bookedBy,
        @NotNull LocalDateTime startsAt,
        @NotNull LocalDateTime endsAt) {}
