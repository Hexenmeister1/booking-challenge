package de.example.booking.booking;

import de.example.booking.common.NotFoundException;
import de.example.booking.resource.Resource;
import de.example.booking.resource.ResourceRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BookingService {

    private final BookingRepository bookings;
    private final ResourceRepository resources;

    BookingService(BookingRepository bookings, ResourceRepository resources) {
        this.bookings = bookings;
        this.resources = resources;
    }

    public BookingResponse create(BookingRequest request) {
        if (!request.startsAt().isBefore(request.endsAt())) {
            throw new InvalidBookingException("Das Ende der Buchung muss nach ihrem Beginn liegen.");
        }

        Resource resource = resources.findByIdForBooking(request.resourceId())
                .orElseThrow(() -> new NotFoundException(
                        "Es gibt keine Ressource mit der ID " + request.resourceId()));

        if (!bookings.findOverlapping(request.resourceId(), request.startsAt(), request.endsAt())
                .isEmpty()) {
            throw new BookingConflictException(
                    resource.getName() + " ist im gewählten Zeitraum bereits gebucht.");
        }

        Booking booking = new Booking(
                resource, request.bookedBy().trim(), request.startsAt(), request.endsAt());
        return BookingResponse.from(bookings.save(booking));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> findFiltered(
            Long resourceId, LocalDateTime from, LocalDateTime to) {
        return bookings.findFiltered(resourceId, from, to).stream()
                .map(BookingResponse::from)
                .toList();
    }

    public void cancel(Long id) {
        if (!bookings.existsById(id)) {
            throw new NotFoundException("Es gibt keine Buchung mit der ID " + id);
        }
        bookings.deleteById(id);
    }
}
