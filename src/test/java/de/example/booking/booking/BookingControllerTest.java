package de.example.booking.booking;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void rejectsOverlappingBookingsButAllowsAdjacentSlotsAndSupportsFilteringAndCancellation()
            throws Exception {
        String firstBooking = """
                {"resourceId":1,"bookedBy":"Ada","startsAt":"2032-04-05T09:00:00","endsAt":"2032-04-05T10:00:00"}
                """;
        MvcResult created = mockMvc.perform(createRequest(firstBooking))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resourceName").value("Besprechungsraum Nord"))
                .andReturn();

        mockMvc.perform(createRequest("""
                        {"resourceId":1,"bookedBy":"Lin","startsAt":"2032-04-05T09:30:00","endsAt":"2032-04-05T10:30:00"}
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Zeitraum nicht verfügbar"))
                .andExpect(jsonPath("$.detail").value(
                        "Besprechungsraum Nord ist im gewählten Zeitraum bereits gebucht."));

        mockMvc.perform(createRequest("""
                        {"resourceId":1,"bookedBy":"Lin","startsAt":"2032-04-05T10:00:00","endsAt":"2032-04-05T11:00:00"}
                        """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/bookings")
                        .param("resourceId", "1")
                        .param("from", "2032-04-05T09:45:00")
                        .param("to", "2032-04-05T10:15:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        Integer firstId = com.jayway.jsonpath.JsonPath.read(
                created.getResponse().getContentAsString(), "$.id");
        mockMvc.perform(delete("/api/bookings/{id}", firstId))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/bookings")
                        .param("resourceId", "1")
                        .param("from", "2032-04-05T00:00:00")
                        .param("to", "2032-04-06T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void rejectsBookingsWhoseEndIsNotAfterTheirStart() throws Exception {
        mockMvc.perform(createRequest("""
                        {"resourceId":1,"bookedBy":"Ada","startsAt":"2032-04-06T10:00:00","endsAt":"2032-04-06T10:00:00"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail")
                        .value("Das Ende der Buchung muss nach ihrem Beginn liegen."));
    }

    @Test
    void allowsOverlappingBookingsForDifferentResourcesAndFiltersIndependently() throws Exception {
        mockMvc.perform(createRequest("""
                        {"resourceId":1,"bookedBy":"Ada","startsAt":"2032-04-07T09:00:00","endsAt":"2032-04-07T10:00:00"}
                        """))
                .andExpect(status().isCreated());
        mockMvc.perform(createRequest("""
                        {"resourceId":1,"bookedBy":"Ada","startsAt":"2032-04-07T12:00:00","endsAt":"2032-04-07T13:00:00"}
                        """))
                .andExpect(status().isCreated());
        mockMvc.perform(createRequest("""
                        {"resourceId":2,"bookedBy":"Lin","startsAt":"2032-04-07T09:00:00","endsAt":"2032-04-07T10:00:00"}
                        """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/bookings").param("resourceId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        mockMvc.perform(get("/api/bookings")
                        .param("from", "2032-04-07T08:00:00")
                        .param("to", "2032-04-07T11:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        mockMvc.perform(get("/api/bookings")
                        .param("resourceId", "1")
                        .param("from", "2032-04-07T08:00:00")
                        .param("to", "2032-04-07T11:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void concurrentRequestsCannotBookTheSameResourceAndTime() throws Exception {
        String request = """
                {"resourceId":1,"bookedBy":"Ada","startsAt":"2032-04-08T09:00:00","endsAt":"2032-04-08T10:00:00"}
                """;
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> performAfterGate(request, ready, start));
            var second = executor.submit(() -> performAfterGate(request, ready, start));
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();

            List<Integer> statuses = List.of(
                    first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS));
            assertEquals(1, statuses.stream().filter(code -> code == 201).count());
            assertEquals(1, statuses.stream().filter(code -> code == 409).count());
        }
    }

    private int performAfterGate(
            String body, CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown();
        if (!start.await(5, TimeUnit.SECONDS)) {
            throw new AssertionError("Concurrent test start timed out");
        }
        return mockMvc.perform(createRequest(body)).andReturn().getResponse().getStatus();
    }

    private MockHttpServletRequestBuilder createRequest(String body) {
        return post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content(body);
    }
}
