package de.example.booking.resource;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Example test for the example feature. It boots the full application, so it also proves that the
 * schema is created and {@code data.sql} is applied.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ResourceControllerTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void returnsAllSeededResourcesSortedByName() throws Exception {
        mockMvc
                .perform(get("/api/resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(8)))
                .andExpect(jsonPath("$[0].name").value("Besprechungsraum Nord"))
                .andExpect(jsonPath("$[0].category").value("ROOM"))
                .andExpect(jsonPath("$[0].capacity").value(8));
    }

    @Test
    void returnsAResourceById() throws Exception {
        mockMvc
                .perform(get("/api/resources/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Besprechungsraum Nord"))
                .andExpect(jsonPath("$.category").value("ROOM"))
                .andExpect(jsonPath("$.location").value("Haus 1, 2. OG"))
                .andExpect(jsonPath("$.capacity").value(8));
    }

    @Test
    void reportsAnUnknownResourceAsProblemDetail() throws Exception {
        mockMvc
                .perform(get("/api/resources/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Nicht gefunden"))
                .andExpect(jsonPath("$.detail").value("Es gibt keine Ressource mit der ID 999999"));
    }

    @Test
    void doesNotUseTheSpaFallbackForUnknownApiRoutes() throws Exception {
        mockMvc.perform(get("/api/not-a-route")).andExpect(status().isNotFound());
    }

    @Test
    void servesTheSpaEntryPointForFrontendDeepLinks() throws Exception {
        mockMvc
                .perform(get("/resources/room/1"))
                .andExpect(status().isOk())
            .andExpect(content().string("<!doctype html><title>test spa entry</title>\n"));
    }

    @Test
    void servesAnExistingStaticResourceDirectly() throws Exception {
        mockMvc
                .perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string("<!doctype html><title>test spa entry</title>\n"));
    }
}
