package de.example.booking.common;

import de.example.booking.booking.BookingConflictException;
import de.example.booking.booking.InvalidBookingException;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Turns exceptions into RFC 9457 {@code application/problem+json} responses, so the frontend always
 * gets a machine-readable body it can show to the user.
 *
 * <p>The frontend counterpart lives in {@code frontend/src/api/client.ts}, which reads {@code
 * detail} and falls back to {@code title}.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail handleNotFound(NotFoundException exception) {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Nicht gefunden");
        return problem;
    }

    @ExceptionHandler(BookingConflictException.class)
    ProblemDetail handleBookingConflict(BookingConflictException exception) {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setTitle("Zeitraum nicht verfügbar");
        return problem;
    }

    @ExceptionHandler(InvalidBookingException.class)
    ProblemDetail handleInvalidBooking(InvalidBookingException exception) {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Ungültiger Buchungszeitraum");
        return problem;
    }

    /** Reports every rejected field, so a form can highlight the offending inputs. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidationFailure(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors =
                exception.getBindingResult().getFieldErrors().stream()
                        .collect(
                                Collectors.toMap(
                                        FieldError::getField,
                                        error ->
                                                error.getDefaultMessage() == null
                                                        ? "ungültig"
                                                        : error.getDefaultMessage(),
                                        (first, second) -> first));

        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST, "Die Eingaben sind unvollständig oder ungültig.");
        problem.setTitle("Ungültige Eingabe");
        problem.setProperty("fieldErrors", fieldErrors);
        return problem;
    }
}
