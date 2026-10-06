package de.example.booking.booking;

import de.example.booking.resource.Resource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Column(nullable = false)
    private String bookedBy;

    @Column(nullable = false)
    private LocalDateTime startsAt;

    @Column(nullable = false)
    private LocalDateTime endsAt;

    protected Booking() {}

    Booking(Resource resource, String bookedBy, LocalDateTime startsAt, LocalDateTime endsAt) {
        this.resource = resource;
        this.bookedBy = bookedBy;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    public Long getId() {
        return id;
    }

    public Resource getResource() {
        return resource;
    }

    public String getBookedBy() {
        return bookedBy;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public LocalDateTime getEndsAt() {
        return endsAt;
    }
}
