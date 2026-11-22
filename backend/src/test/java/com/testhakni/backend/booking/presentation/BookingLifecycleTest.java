package com.testhakni.backend.booking.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.testhakni.backend.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Le client pilote sa reservation : en cours, terminee ou annulee ; l'experience du prestataire suit. */
class BookingLifecycleTest extends IntegrationTest {

    private JsonNode call(String token, MockHttpServletRequestBuilder req, int expected) throws Exception {
        String body = mockMvc.perform(withToken(req, token)).andExpect(status().is(expected))
                .andReturn().getResponse().getContentAsString();
        return body.isBlank() ? null : readJson(body);
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder req, Object payload) throws Exception {
        return req.contentType(MediaType.APPLICATION_JSON).content(json(payload));
    }

    /** Cree une demande, une offre et l'accepte : renvoie l'id de la reservation. */
    private String booked(String customer, String provider) throws Exception {
        String request = call(customer, json(post("/api/requests"), Map.of("title", "Fuite cuisine", "description",
                "Réparer la fuite dans la cuisine", "category", "PLOMBERIE", "city", "Tunis")), 201).get("id").asText();
        String offer = call(provider, json(post("/api/requests/" + request + "/offers"), Map.of("price", 75,
                "proposedDate", LocalDate.now().plusDays(2).toString(), "description", "Réparation complète")), 201)
                .get("id").asText();
        return call(customer, post("/api/requests/" + request + "/offers/" + offer + "/accept"), 200).get("id").asText();
    }

    private JsonNode setStatus(String token, String booking, String status, int expected) throws Exception {
        return call(token, json(patch("/api/bookings/" + booking + "/customer-status"), Map.of("status", status)), expected);
    }

    private String requestStatus(String customer, String bookingId) throws Exception {
        String requestId = call(customer, get("/api/bookings/" + bookingId), 200).get("requestId").asText();
        return call(customer, get("/api/requests/" + requestId), 200).get("status").asText();
    }

    @Test
    void customerStartsThenCompletesAndProviderGainsExperience() throws Exception {
        String customer = token(registerCustomer("client@test.dev"));
        var providerAuth = registerProvider("pro@test.dev");
        String provider = token(providerAuth);
        String booking = booked(customer, provider);

        assertThat(setStatus(customer, booking, "IN_PROGRESS", 200).get("status").asText()).isEqualTo("IN_PROGRESS");
        setStatus(customer, booking, "IN_PROGRESS", 409); // deja en cours

        String publicProfile = "/api/providers/" + userId(providerAuth);
        assertThat(call(customer, get(publicProfile), 200).get("completedJobs").asLong()).isZero();

        JsonNode done = setStatus(customer, booking, "COMPLETED", 200);
        assertThat(done.get("status").asText()).isEqualTo("COMPLETED");
        assertThat(done.get("history")).hasSize(3);
        assertThat(requestStatus(customer, booking)).isEqualTo("COMPLETED");
        assertThat(call(customer, get(publicProfile), 200).get("completedJobs").asLong()).isEqualTo(1);

        // Le prestataire est prevenu, et le client peut ensuite laisser son avis
        assertThat(call(provider, get("/api/notifications"), 200).get("content").toString()).contains("BOOKING_COMPLETED");
        call(customer, json(post("/api/bookings/" + booking + "/review"), Map.of("rating", 5, "comment", "Très bien")), 201);
        // Une prestation terminee ne s'annule plus
        setStatus(customer, booking, "CANCELLED", 409);
    }

    @Test
    void customerCanCancelBeforeCompletionAndTheRequestFollows() throws Exception {
        String customer = token(registerCustomer("client@test.dev"));
        var providerAuth = registerProvider("pro@test.dev");
        String provider = token(providerAuth);
        String booking = booked(customer, provider);

        assertThat(setStatus(customer, booking, "CANCELLED", 200).get("status").asText()).isEqualTo("CANCELLED");
        assertThat(requestStatus(customer, booking)).isEqualTo("CANCELLED");
        assertThat(call(provider, get("/api/notifications"), 200).get("content").toString()).contains("BOOKING_CANCELLED");
        assertThat(call(customer, get("/api/providers/" + userId(providerAuth)), 200).get("completedJobs").asLong()).isZero();

        // Plus aucune action possible, ni pour le client ni pour le prestataire ; pas d'avis
        setStatus(customer, booking, "COMPLETED", 409);
        call(provider, json(patch("/api/bookings/" + booking + "/status"), Map.of("status", "IN_PROGRESS")), 409);
        call(customer, json(post("/api/bookings/" + booking + "/review"), Map.of("rating", 4, "comment", "x")), 409);
    }

    @Test
    void onlyTheBookingsCustomerCanDriveTheStatus() throws Exception {
        String customer = token(registerCustomer("client@test.dev"));
        String stranger = token(registerCustomer("autre@test.dev"));
        String provider = token(registerProvider("pro@test.dev"));
        String booking = booked(customer, provider);

        setStatus(provider, booking, "COMPLETED", 403);   // role prestataire refuse
        setStatus(stranger, booking, "CANCELLED", 403);   // client d'une autre reservation
        setStatus(customer, booking, "BOOKED", 400);     // statut non autorise
        setStatus(null, booking, "COMPLETED", 401);
        assertThat(call(customer, get("/api/bookings/" + booking), 200).get("status").asText()).isEqualTo("BOOKED");
    }

    @Test
    void providerCanStillStartAndDeclareDoneThenCustomerConfirms() throws Exception {
        String customer = token(registerCustomer("client@test.dev"));
        String provider = token(registerProvider("pro@test.dev"));
        String booking = booked(customer, provider);

        call(provider, json(patch("/api/bookings/" + booking + "/status"), Map.of("status", "IN_PROGRESS")), 200);
        call(provider, json(patch("/api/bookings/" + booking + "/status"), Map.of("status", "AWAITING_CONFIRMATION")), 200);
        assertThat(call(customer, post("/api/bookings/" + booking + "/confirm"), 200).get("status").asText()).isEqualTo("COMPLETED");
    }
}
