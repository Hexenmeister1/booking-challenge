package de.example.booking.booking;

public class InvalidBookingException extends RuntimeException {

    public InvalidBookingException(String message) {
        super(message);
    }
}
