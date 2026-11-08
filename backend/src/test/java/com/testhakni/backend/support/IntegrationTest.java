package com.testhakni.backend.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Base des tests d'integration API (TEST-00) :
 * contexte Spring complet, profil "test" (base testhakni_test), MockMvc,
 * et base videe avant chaque test (les index sont conserves).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTest {

    public static final String PASSWORD = "Secret123!";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getCollectionNames().stream()
                .filter(name -> !name.startsWith("system."))
                .forEach(name -> mongoTemplate.remove(new Query(), name));
    }

    protected String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    protected JsonNode readJson(String content) throws Exception {
        return objectMapper.readTree(content);
    }

    /** POST JSON avec un token optionnel. */
    protected MockHttpServletRequestBuilder postJson(String url, Object body, String token) throws Exception {
        return withToken(post(url).contentType(MediaType.APPLICATION_JSON).content(json(body)), token);
    }

    protected MockHttpServletRequestBuilder withToken(MockHttpServletRequestBuilder req, String token) {
        return token == null ? req : req.header("Authorization", "Bearer " + token);
    }

    /** Inscrit un client et renvoie la reponse d'auth (accessToken, user.id...). */
    protected JsonNode registerCustomer(String email) throws Exception {
        Map<String, Object> body = Map.of("email", email, "password", PASSWORD,
                "firstName", "Client", "lastName", "Test", "city", "Tunis");
        String res = mockMvc.perform(postJson("/api/auth/register/customer", body, null))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return readJson(res);
    }

    protected JsonNode registerProvider(String email) throws Exception {
        Map<String, Object> body = Map.of("email", email, "password", PASSWORD,
                "firstName", "Presta", "lastName", "Test", "phone", "+216 20 123 456", "city", "Tunis",
                "businessName", "Presta " + email, "categories", List.of("PLOMBERIE"));
        String res = mockMvc.perform(postJson("/api/auth/register/provider", body, null))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return readJson(res);
    }

    protected static String token(JsonNode auth) {
        return auth.get("accessToken").asText();
    }

    protected static String userId(JsonNode auth) {
        return auth.get("user").get("id").asText();
    }
}
