package com.testhakni.backend.media.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import com.fasterxml.jackson.databind.JsonNode;
import com.testhakni.backend.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;

/** Photos de profil et de realisations : formats reels, taille, limite et propriete. SEC-08 */
class MediaControllerTest extends IntegrationTest {

    /** Signature PNG valide suivie de quelques octets : suffit a la detection du format. */
    static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 13, 'I', 'H', 'D', 'R'};

    private static MockMultipartFile file(byte[] content, String name, String type) {
        return new MockMultipartFile("file", name, type, content);
    }

    private JsonNode upload(HttpMethod method, String url, MockMultipartFile file, String token, int expected)
            throws Exception {
        var req = multipart(method, url).file(file);
        String body = mockMvc.perform(withToken(req, token)).andExpect(status().is(expected))
                .andReturn().getResponse().getContentAsString();
        return body.isBlank() ? null : readJson(body);
    }

    @Test
    void userCanSetAvatarAndAnyoneCanDisplayIt() throws Exception {
        String t = token(registerCustomer("avatar@mail.com"));
        JsonNode me = upload(HttpMethod.PUT, "/api/users/me/avatar", file(PNG, "moi.png", "image/png"), t, 200);
        String url = me.get("avatarUrl").asText();
        assertThat(url).startsWith("/api/media/");

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(content().bytes(PNG));

        // Remplacer la photo supprime l'ancienne
        JsonNode replaced = upload(HttpMethod.PUT, "/api/users/me/avatar", file(PNG, "b.png", "image/png"), t, 200);
        assertThat(replaced.get("avatarUrl").asText()).isNotEqualTo(url);
        mockMvc.perform(get(url)).andExpect(status().isNotFound());
    }

    @Test
    void fakeImagesAndHugeFilesAreRejected() throws Exception {
        String t = token(registerCustomer("fake@mail.com"));
        // Un script deguise en PNG : l'extension et le Content-Type mentent, la signature non
        byte[] script = "<script>alert(1)</script>".getBytes();
        upload(HttpMethod.PUT, "/api/users/me/avatar", file(script, "photo.png", "image/png"), t, 400);

        byte[] huge = Arrays.copyOf(PNG, 2 * 1024 * 1024 + 10);
        int status = mockMvc.perform(withToken(multipart(HttpMethod.PUT, "/api/users/me/avatar")
                .file(file(huge, "big.png", "image/png")), t)).andReturn().getResponse().getStatus();
        assertThat(status).isIn(400, 413);

        upload(HttpMethod.PUT, "/api/users/me/avatar", file(PNG, "x.png", "image/png"), null, 401);
    }

    @Test
    void providerPortfolioIsLimitedAndOwned() throws Exception {
        String pro = token(registerProvider("portfolio@mail.com"));
        String other = token(registerProvider("autre@mail.com"));
        String customer = token(registerCustomer("client@mail.com"));

        JsonNode profile = null;
        for (int i = 0; i < 6; i++) {
            profile = upload(HttpMethod.POST, "/api/providers/me/photos", file(PNG, "p.png", "image/png"), pro, 200);
        }
        assertThat(profile.get("photoUrls")).hasSize(6);
        upload(HttpMethod.POST, "/api/providers/me/photos", file(PNG, "p.png", "image/png"), pro, 409);
        upload(HttpMethod.POST, "/api/providers/me/photos", file(PNG, "p.png", "image/png"), customer, 403);

        String photoId = profile.get("photoUrls").get(0).asText().replace("/api/media/", "");
        mockMvc.perform(withToken(delete("/api/providers/me/photos/" + photoId), other)).andExpect(status().isNotFound());
        mockMvc.perform(withToken(delete("/api/providers/me/photos/" + photoId), pro)).andExpect(status().isOk());
        mockMvc.perform(get("/api/media/" + photoId)).andExpect(status().isNotFound());
    }
}
