package com.testhakni.backend.contact.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.testhakni.backend.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Contacts client / prestataire : demande, acceptation, discussion directe, retrait. */
class ContactControllerTest extends IntegrationTest {

    private JsonNode call(String token, MockHttpServletRequestBuilder req, int expected) throws Exception {
        String body = mockMvc.perform(withToken(req, token)).andExpect(status().is(expected))
                .andReturn().getResponse().getContentAsString();
        return body.isBlank() ? null : readJson(body);
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder req, Object payload) throws Exception {
        return req.contentType(MediaType.APPLICATION_JSON).content(json(payload));
    }

    private JsonNode requestContact(String token, String targetId, int expected) throws Exception {
        return call(token, json(post("/api/contacts"), Map.of("targetId", targetId)), expected);
    }

    @Test
    void acceptedContactsCanChatWithoutAJobRequest() throws Exception {
        var customerAuth = registerCustomer("client@test.dev");
        var providerAuth = registerProvider("pro@test.dev");
        String customer = token(customerAuth);
        String provider = token(providerAuth);
        String providerId = userId(providerAuth);

        // Pas encore en contact : discussion directe refusee
        call(customer, post("/api/conversations/with/" + providerId), 403);

        JsonNode sent = requestContact(customer, providerId, 201);
        assertThat(sent.get("status").asText()).isEqualTo("PENDING");
        assertThat(call(customer, get("/api/contacts/with/" + providerId), 200).get("state").asText()).isEqualTo("PENDING_OUT");
        assertThat(call(provider, get("/api/contacts/pending"), 200).get("incoming").asLong()).isEqualTo(1);

        // Seul le destinataire peut accepter
        String contactId = sent.get("id").asText();
        call(customer, post("/api/contacts/" + contactId + "/accept"), 403);
        JsonNode accepted = call(provider, post("/api/contacts/" + contactId + "/accept"), 200);
        assertThat(accepted.get("status").asText()).isEqualTo("ACCEPTED");
        assertThat(accepted.get("otherName").asText()).isEqualTo("Client T.");

        // Discussion directe : meme fil pour les deux, sans demande de travail
        JsonNode conv = call(customer, post("/api/conversations/with/" + providerId), 200);
        assertThat(conv.get("requestId").isNull()).isTrue();
        String fromProvider = call(provider, post("/api/conversations/with/" + userId(customerAuth)), 200).get("id").asText();
        assertThat(fromProvider).isEqualTo(conv.get("id").asText());
        call(provider, json(post("/api/conversations/" + fromProvider + "/messages"), Map.of("body", "Merci pour l'ajout !")), 201);

        // Contact retire : le fil direct se ferme
        call(customer, delete("/api/contacts/" + contactId), 204);
        call(provider, get("/api/conversations/" + fromProvider + "/messages"), 403);
    }

    @Test
    void crossedRequestsAcceptEachOtherAndRepeatsAreIdempotent() throws Exception {
        var customerAuth = registerCustomer("client@test.dev");
        var providerAuth = registerProvider("pro@test.dev");
        String customer = token(customerAuth);
        String provider = token(providerAuth);

        String id = requestContact(customer, userId(providerAuth), 201).get("id").asText();
        assertThat(requestContact(customer, userId(providerAuth), 201).get("id").asText()).isEqualTo(id);
        // Le prestataire demande a son tour : la demande croisee vaut acceptation
        assertThat(requestContact(provider, userId(customerAuth), 201).get("status").asText()).isEqualTo("ACCEPTED");
        assertThat(call(customer, get("/api/contacts"), 200)).hasSize(1);
    }

    @Test
    void contactsOnlyLinkACustomerAndAProvider() throws Exception {
        var a = registerCustomer("a@test.dev");
        var b = registerCustomer("b@test.dev");
        requestContact(token(a), userId(b), 400);
        requestContact(token(a), userId(a), 400);
        requestContact(token(a), "inconnu", 404);
        call(null, get("/api/contacts"), 401);
    }

    @Test
    void declinedRequestIsHiddenFromTheTargetAndCanBeRenewed() throws Exception {
        var customerAuth = registerCustomer("client@test.dev");
        var providerAuth = registerProvider("pro@test.dev");
        String customer = token(customerAuth);
        String provider = token(providerAuth);

        String id = requestContact(customer, userId(providerAuth), 201).get("id").asText();
        assertThat(call(provider, post("/api/contacts/" + id + "/decline"), 200).get("status").asText()).isEqualTo("DECLINED");
        call(provider, post("/api/contacts/" + id + "/accept"), 409);
        assertThat(call(provider, get("/api/contacts"), 200)).isEmpty();
        assertThat(call(customer, get("/api/contacts/with/" + userId(providerAuth)), 200).get("state").asText()).isEqualTo("DECLINED");

        assertThat(requestContact(customer, userId(providerAuth), 201).get("status").asText()).isEqualTo("PENDING");
    }
}
