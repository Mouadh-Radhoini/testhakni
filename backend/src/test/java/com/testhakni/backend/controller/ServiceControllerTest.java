package com.testhakni.backend.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.testhakni.backend.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/** Tests API : SERV-01 a SERV-05, SEARCH-01 a SEARCH-04, PROV-02/03, SEC-02. */
class ServiceControllerTest extends IntegrationTest {

    private Map<String, Object> service(String title, String category, String city, String price) {
        Map<String, Object> m = new HashMap<>();
        m.put("title", title);
        m.put("description", "Description detaillee du service " + title);
        m.put("category", category);
        m.put("city", city);
        m.put("price", price);
        m.put("priceType", "FIXED");
        return m;
    }

    private String createService(String token, Map<String, Object> body) throws Exception {
        String res = mockMvc.perform(postJson("/api/services", body, token))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return readJson(res).get("id").asText();
    }

    @Test
    void providerCreatesServiceWithProviderSummary() throws Exception {
        String t = token(registerProvider("p1@mail.com"));
        mockMvc.perform(postJson("/api/services", service("Fuite d'eau", "PLOMBERIE", "Tunis", "60"), t))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.categoryLabel").value("Plomberie"))
                .andExpect(jsonPath("$.price").value(60))
                .andExpect(jsonPath("$.provider.businessName").value("Presta p1@mail.com"));
    }

    @Test
    void anonymousCannotCreateAndValidationIsDetailed() throws Exception {
        mockMvc.perform(postJson("/api/services", service("Titre", "PLOMBERIE", "Tunis", "10"), null))
                .andExpect(status().isUnauthorized());

        String t = token(registerProvider("p2@mail.com"));
        Map<String, Object> bad = service("x", null, "", null);
        mockMvc.perform(postJson("/api/services", bad, t))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.category").exists())
                .andExpect(jsonPath("$.fieldErrors.city").exists())
                .andExpect(jsonPath("$.fieldErrors.priceProvided").exists());
    }

    @Test
    void onlyOwnerCanUpdateChangeStatusOrDelete() throws Exception {
        String owner = token(registerProvider("owner@mail.com"));
        String other = token(registerProvider("other@mail.com"));
        String id = createService(owner, service("Peinture salon", "PEINTURE", "Tunis", "200"));

        mockMvc.perform(withToken(put("/api/services/" + id), other).contentType(MediaType.APPLICATION_JSON)
                        .content(json(service("Vole", "PEINTURE", "Tunis", "1"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(withToken(delete("/api/services/" + id), other))
                .andExpect(status().isForbidden());

        mockMvc.perform(withToken(put("/api/services/" + id), owner).contentType(MediaType.APPLICATION_JSON)
                        .content(json(service("Peinture salon et couloir", "PEINTURE", "Tunis", "250"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Peinture salon et couloir"));

        mockMvc.perform(withToken(patch("/api/services/" + id + "/status"), owner).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "INACTIVE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        // Un service inactif n'est plus visible publiquement, mais l'est par son proprietaire
        mockMvc.perform(get("/api/services/" + id)).andExpect(status().isNotFound());
        mockMvc.perform(withToken(get("/api/services/" + id), owner)).andExpect(status().isOk());
        mockMvc.perform(withToken(get("/api/services/mine"), owner))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(withToken(delete("/api/services/" + id), owner)).andExpect(status().isNoContent());
        mockMvc.perform(withToken(get("/api/services/" + id), owner)).andExpect(status().isNotFound());
    }

    @Test
    void searchFiltersCanBeCombinedAndPaginated() throws Exception {
        String t = token(registerProvider("search@mail.com"));
        createService(t, service("Reparation fuite", "PLOMBERIE", "Tunis", "60"));
        createService(t, service("Chauffe-eau", "PLOMBERIE", "Sousse", "150"));
        createService(t, service("Menage complet", "MENAGE", "Tunis", "20"));
        String hidden = createService(t, service("Fuite cachee", "PLOMBERIE", "Tunis", "70"));
        mockMvc.perform(withToken(patch("/api/services/" + hidden + "/status"), t).contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("status", "INACTIVE")))).andExpect(status().isOk());

        mockMvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));
        mockMvc.perform(get("/api/services").param("q", "FUITE"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Reparation fuite"));
        mockMvc.perform(get("/api/services").param("category", "PLOMBERIE").param("city", "tun"))
                .andExpect(jsonPath("$.totalElements").value(1));
        mockMvc.perform(get("/api/services").param("minPrice", "50").param("maxPrice", "200").param("sort", "PRICE_DESC"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Chauffe-eau"));
        mockMvc.perform(get("/api/services").param("size", "2").param("page", "1"))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.last").value(true));
        mockMvc.perform(get("/api/services").param("q", "introuvable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void invalidFiltersReturn400() throws Exception {
        mockMvc.perform(get("/api/services").param("category", "INCONNUE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.category").exists());
        mockMvc.perform(get("/api/services").param("minPrice", "100").param("maxPrice", "10"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/services").param("size", "500"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/services").param("minPrice", "-5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void publicProviderProfileHidesPrivateData() throws Exception {
        var auth = registerProvider("public@mail.com");
        String t = token(auth);
        createService(t, service("Fuite", "PLOMBERIE", "Tunis", "60"));
        mockMvc.perform(withToken(put("/api/providers/me/profile"), t).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("businessName", "Pro Plomberie", "bio", "Experience solide",
                                "categories", List.of("PLOMBERIE", "ELECTRICITE"), "skills", List.of("Fuites"),
                                "city", "Tunis", "yearsOfExperience", 8))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessName").value("Pro Plomberie"));

        mockMvc.perform(get("/api/providers/" + userId(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessName").value("Pro Plomberie"))
                .andExpect(jsonPath("$.displayName").value("Presta T."))
                .andExpect(jsonPath("$.services", hasSize(1)))
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.phone").doesNotExist());

        mockMvc.perform(get("/api/providers/inexistant")).andExpect(status().isNotFound());
    }

    @Test
    void providerProfileValidation() throws Exception {
        String t = token(registerProvider("val@mail.com"));
        mockMvc.perform(withToken(put("/api/providers/me/profile"), t).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("businessName", "", "categories", List.of(), "city", "",
                                "yearsOfExperience", -1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.businessName").exists())
                .andExpect(jsonPath("$.fieldErrors.categories").exists())
                .andExpect(jsonPath("$.fieldErrors.yearsOfExperience").exists());
    }
}
