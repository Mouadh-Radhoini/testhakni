package com.testhakni.backend.messaging.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.testhakni.backend.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Messagerie client / prestataire : ouverture, envoi, non-lus et confidentialite (SEC-02). */
class ConversationControllerTest extends IntegrationTest {

    private JsonNode call(String token, MockHttpServletRequestBuilder req, int expected) throws Exception {
        String body = mockMvc.perform(withToken(req, token)).andExpect(status().is(expected))
                .andReturn().getResponse().getContentAsString();
        return body.isBlank() ? null : readJson(body);
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder req, Object payload) throws Exception {
        return req.contentType(MediaType.APPLICATION_JSON).content(json(payload));
    }

    private String request(String customer) throws Exception {
        return call(customer, json(post("/api/requests"), Map.of("title", "Fuite cuisine", "description",
                "Réparer la fuite dans la cuisine", "category", "PLOMBERIE", "city", "Tunis")), 201).get("id").asText();
    }

    private String offer(String provider, String request) throws Exception {
        return call(provider, json(post("/api/requests/" + request + "/offers"), Map.of("price", 75,
                "proposedDate", LocalDate.now().plusDays(2).toString(), "description", "Réparation complète")), 201)
                .get("id").asText();
    }

    @Test
    void customerAndProviderChatAroundAnOffer() throws Exception {
        var customerAuth = registerCustomer("client@test.dev");
        String customer = token(customerAuth);
        var providerAuth = registerProvider("pro@test.dev");
        String provider = token(providerAuth);
        String offerId = offer(provider, request(customer));

        // Les deux participants retrouvent le meme fil
        String conv = call(customer, post("/api/conversations/by-offer/" + offerId), 200).get("id").asText();
        JsonNode fromProvider = call(provider, post("/api/conversations/by-offer/" + offerId), 200);
        assertThat(fromProvider.get("id").asText()).isEqualTo(conv);
        assertThat(fromProvider.get("title").asText()).isEqualTo("Fuite cuisine");

        JsonNode sent = call(customer, json(post("/api/conversations/" + conv + "/messages"),
                Map.of("body", "  Bonjour, êtes-vous disponible jeudi ?  ")), 201);
        assertThat(sent.get("body").asText()).isEqualTo("Bonjour, êtes-vous disponible jeudi ?");
        assertThat(sent.get("senderId").asText()).isEqualTo(userId(customerAuth));

        // Le prestataire a un message non lu ; l'expediteur aucun
        assertThat(call(provider, get("/api/conversations/unread"), 200).get("unread").asLong()).isEqualTo(1);
        assertThat(call(customer, get("/api/conversations/unread"), 200).get("unread").asLong()).isZero();
        JsonNode inbox = call(provider, get("/api/conversations"), 200);
        assertThat(inbox.get(0).get("otherParticipantName").asText()).isEqualTo("Client T.");
        assertThat(inbox.get(0).get("lastMessagePreview").asText()).startsWith("Bonjour");

        call(provider, json(post("/api/conversations/" + conv + "/messages"), Map.of("body", "Oui, à 9 h.")), 201);
        call(provider, post("/api/conversations/" + conv + "/read"), 204);
        assertThat(call(provider, get("/api/conversations/unread"), 200).get("unread").asLong()).isZero();

        JsonNode history = call(customer, get("/api/conversations/" + conv + "/messages"), 200);
        assertThat(history).hasSize(2);
        assertThat(history.get(1).get("body").asText()).isEqualTo("Oui, à 9 h.");
    }

    @Test
    void outsidersCannotOpenReadOrWriteAConversation() throws Exception {
        String customer = token(registerCustomer("client@test.dev"));
        String provider = token(registerProvider("pro@test.dev"));
        String strangerCustomer = token(registerCustomer("autre@test.dev"));
        String strangerProvider = token(registerProvider("concurrent@test.dev"));
        String offerId = offer(provider, request(customer));
        String conv = call(customer, post("/api/conversations/by-offer/" + offerId), 200).get("id").asText();

        call(strangerCustomer, post("/api/conversations/by-offer/" + offerId), 403);
        call(strangerProvider, get("/api/conversations/" + conv + "/messages"), 403);
        call(strangerCustomer, json(post("/api/conversations/" + conv + "/messages"), Map.of("body", "Spam")), 403);
        call(customer, get("/api/conversations/inconnue/messages"), 404);
        call(null, get("/api/conversations"), 401);
    }

    @Test
    void emptyOrTooLongMessagesAreRejected() throws Exception {
        String customer = token(registerCustomer("client@test.dev"));
        String provider = token(registerProvider("pro@test.dev"));
        String conv = call(customer, post("/api/conversations/by-offer/" + offer(provider, request(customer))), 200)
                .get("id").asText();
        call(customer, json(post("/api/conversations/" + conv + "/messages"), Map.of("body", "   ")), 400);
        call(customer, json(post("/api/conversations/" + conv + "/messages"), Map.of("body", "x".repeat(2001))), 400);
    }
}
