package de.example.booking.booking;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            select booking from Booking booking
            where booking.resource.id = :resourceId
              and booking.startsAt < :endsAt
              and booking.endsAt > :startsAt
            """)
    List<Booking> findOverlapping(
            @Param("resourceId") Long resourceId,
            @Param("startsAt") LocalDateTime startsAt,
            @Param("endsAt") LocalDateTime endsAt);

    @Query("""
            select booking from Booking booking
            where (:resourceId is null or booking.resource.id = :resourceId)
              and (:from is null or booking.endsAt > :from)
              and (:to is null or booking.startsAt < :to)
            order by booking.startsAt
            """)
    List<Booking> findFiltered(
            @Param("resourceId") Long resourceId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
