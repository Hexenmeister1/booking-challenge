package de.example.booking.booking;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import de.example.booking.common.ApiProblem;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Buchungen", description = "Buchungen anlegen, suchen und stornieren.")
public class BookingController {

    private final BookingService bookings;

    BookingController(BookingService bookings) {
        this.bookings = bookings;
    }

    @GetMapping
        @Operation(
            summary = "Buchungen suchen",
            description = "Filtert Buchungen optional nach Ressource und überlappendem Zeitraum.")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Gefilterte Buchungen"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ungültiges Datums-/Uhrzeitformat",
                    content = @Content(schema = @Schema(implementation = ApiProblem.class)))
        })
    public List<BookingResponse> findFiltered(
            @Parameter(description = "Ressourcen-ID") @RequestParam(required = false) Long resourceId,
            @Parameter(description = "Untergrenze; Buchungen müssen danach enden")
                @RequestParam(required = false)
                LocalDateTime from,
            @Parameter(description = "Obergrenze; Buchungen müssen davor beginnen")
                @RequestParam(required = false)
                LocalDateTime to) {
        return bookings.findFiltered(resourceId, from, to);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
        @Operation(
            summary = "Buchung anlegen",
            description = "Erstellt eine Buchung, sofern die Ressource im Zeitraum verfügbar ist.")
        @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Buchung angelegt",
            content = @Content(schema = @Schema(implementation = BookingResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Ungültige Eingaben oder ungültiger Zeitraum",
            content = @Content(schema = @Schema(implementation = ApiProblem.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Ressource nicht gefunden",
            content = @Content(schema = @Schema(implementation = ApiProblem.class))),
        @ApiResponse(
            responseCode = "409",
            description = "Ressource ist im gewünschten Zeitraum bereits gebucht",
            content = @Content(schema = @Schema(implementation = ApiProblem.class)))
        })
    public BookingResponse create(@Valid @RequestBody BookingRequest request) {
        return bookings.create(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
        @Operation(summary = "Buchung stornieren")
        @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Buchung storniert"),
        @ApiResponse(
            responseCode = "404",
            description = "Buchung nicht gefunden",
            content = @Content(schema = @Schema(implementation = ApiProblem.class)))
        })
    public void cancel(@PathVariable Long id) {
        bookings.cancel(id);
    }
}
