package de.example.booking.resource;

import de.example.booking.common.ApiProblem;
import de.example.booking.common.NotFoundException;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only API for bookable resources — the example feature of this template.
 *
 * <p>Nothing here is binding for the exercise: name your own endpoints however you see fit.
 */
@RestController
@RequestMapping("/api/resources")
@Tag(name = "Ressourcen", description = "Die im System buchbaren Ressourcen.")
public class ResourceController {

    private final ResourceRepository resources;

    ResourceController(ResourceRepository resources) {
        this.resources = resources;
    }

    @GetMapping
    @Operation(summary = "Alle Ressourcen anzeigen")
    @ApiResponse(responseCode = "200", description = "Ressourcen alphabetisch sortiert")
    public List<ResourceResponse> findAll() {
        return resources.findAllByOrderByNameAsc().stream().map(ResourceResponse::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ressource anzeigen")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ressource gefunden"),
        @ApiResponse(
                responseCode = "404",
                description = "Ressource nicht gefunden",
                content = @Content(schema = @Schema(implementation = ApiProblem.class)))
    })
    public ResourceResponse findById(@PathVariable Long id) {
        return resources
                .findById(id)
                .map(ResourceResponse::from)
                .orElseThrow(() -> new NotFoundException("Es gibt keine Ressource mit der ID " + id));
    }
}
