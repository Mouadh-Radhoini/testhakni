package com.testhakni.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.testhakni.backend.support.IntegrationTest;
import org.junit.jupiter.api.Test;

class HealthControllerTest extends IntegrationTest {

    @Test
    void healthReturnsUp() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").value("testhakni-backend"));
    }

    @Test
    void categoriesArePublic() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("PLOMBERIE"))
                .andExpect(jsonPath("$[0].label").value("Plomberie"));
    }

    @Test
    void unknownRouteRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/does-not-exist"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}
