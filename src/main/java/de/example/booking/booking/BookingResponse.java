package de.example.booking.booking;

import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long resourceId,
        String resourceName,
        String bookedBy,
        LocalDateTime startsAt,
        LocalDateTime endsAt) {

    static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getResource().getId(),
                booking.getResource().getName(),
                booking.getBookedBy(),
                booking.getStartsAt(),
                booking.getEndsAt());
    }
}
