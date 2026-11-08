package com.testhakni.backend.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.testhakni.backend.support.IntegrationTest;
import org.junit.jupiter.api.Test;

/** Tests API : REQ-01 / REQ-02, SEC-01 / SEC-02. */
class RequestControllerTest extends IntegrationTest {

    private Map<String, Object> request(String title) {
        Map<String, Object> m = new HashMap<>();
        m.put("title", title);
        m.put("description", "Description detaillee du besoin du client");
        m.put("category", "PLOMBERIE");
        m.put("city", "Tunis");
        m.put("preferredDate", LocalDate.now().plusDays(2).toString());
        m.put("budget", 80);
        return m;
    }

    private String create(String token, String title) throws Exception {
        String res = mockMvc.perform(postJson("/api/requests", request(title), token))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return readJson(res).get("id").asText();
    }

    @Test
    void customerCreatesRequestLinkedToHim() throws Exception {
        var auth = registerCustomer("req@mail.com");
        mockMvc.perform(postJson("/api/requests", request("Fuite cuisine"), token(auth)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.customerId").value(userId(auth)))
                .andExpect(jsonPath("$.categoryLabel").value("Plomberie"));
    }

    @Test
    void onlyCustomersCanCreateRequests() throws Exception {
        mockMvc.perform(postJson("/api/requests", request("Fuite"), null)).andExpect(status().isUnauthorized());
        String provider = token(registerProvider("prov@mail.com"));
        mockMvc.perform(postJson("/api/requests", request("Fuite"), provider)).andExpect(status().isForbidden());
    }

    @Test
    void validationErrorsAreExplicit() throws Exception {
        String t = token(registerCustomer("v@mail.com"));
        Map<String, Object> bad = request("x");
        bad.put("category", null);
        bad.put("preferredDate", LocalDate.now().minusDays(1).toString());
        bad.put("budget", -10);
        mockMvc.perform(postJson("/api/requests", bad, t))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.category").exists())
                .andExpect(jsonPath("$.fieldErrors.preferredDate").exists())
                .andExpect(jsonPath("$.fieldErrors.budget").exists());
    }

    @Test
    void customerSeesOnlyOwnRequestsAndCanCancel() throws Exception {
        String alice = token(registerCustomer("alice@mail.com"));
        String bob = token(registerCustomer("bob@mail.com"));
        String aliceReq = create(alice, "Demande Alice");
        create(alice, "Autre demande Alice");
        create(bob, "Demande Bob");

        mockMvc.perform(withToken(get("/api/requests/mine"), alice))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
        mockMvc.perform(withToken(get("/api/requests/" + aliceReq), bob))
                .andExpect(status().isForbidden());
        mockMvc.perform(withToken(patch("/api/requests/" + aliceReq + "/cancel"), bob))
                .andExpect(status().isForbidden());

        mockMvc.perform(withToken(patch("/api/requests/" + aliceReq + "/cancel"), alice))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
        mockMvc.perform(withToken(patch("/api/requests/" + aliceReq + "/cancel"), alice))
                .andExpect(status().isConflict());

        mockMvc.perform(withToken(get("/api/requests/mine").param("status", "OPEN"), alice))
                .andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(withToken(get("/api/requests/missing"), alice)).andExpect(status().isNotFound());
    }

    @Test
    void requestWithUnknownServiceIsRejected() throws Exception {
        String t = token(registerCustomer("s@mail.com"));
        Map<String, Object> body = request("Depuis un service");
        body.put("serviceId", "inexistant");
        mockMvc.perform(postJson("/api/requests", body, t)).andExpect(status().isBadRequest());
    }
}
