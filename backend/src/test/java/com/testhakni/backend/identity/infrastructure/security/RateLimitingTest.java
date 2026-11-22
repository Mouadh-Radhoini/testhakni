package com.testhakni.backend.identity.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testhakni.backend.identity.infrastructure.security.RateLimitProperties.Rule;
import com.testhakni.backend.shared.domain.exception.ErrorType;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** SEC-04 : limitation par IP sur /api/auth et blocage temporaire d'un compte apres trop d'echecs. */
class RateLimitingTest {

    private static RateLimitProperties props(int capacity) {
        Rule rule = new Rule(capacity, Duration.ofMinutes(1));
        return new RateLimitProperties(true, rule, rule, rule, rule);
    }

    private static MockHttpServletResponse call(AuthRateLimitFilter filter, String method, String uri, String ip)
            throws Exception {
        var request = new MockHttpServletRequest(method, uri);
        request.setRemoteAddr(ip);
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    @Test
    void loginIsLimitedPerIpAndAnswers429WithRetryAfter() throws Exception {
        var filter = new AuthRateLimitFilter(props(2), new ObjectMapper().findAndRegisterModules());
        assertThat(call(filter, "POST", "/api/auth/login", "10.0.0.1").getStatus()).isEqualTo(200);
        assertThat(call(filter, "POST", "/api/auth/login", "10.0.0.1").getStatus()).isEqualTo(200);

        var blocked = call(filter, "POST", "/api/auth/login", "10.0.0.1");
        assertThat(blocked.getStatus()).isEqualTo(429);
        assertThat(blocked.getHeader("Retry-After")).isNotBlank();
        assertThat(blocked.getContentAsString()).contains("Trop de tentatives");

        // Une autre adresse IP garde son propre credit
        assertThat(call(filter, "POST", "/api/auth/login", "10.0.0.2").getStatus()).isEqualTo(200);
    }

    @Test
    void onlySensitiveAuthCallsAreLimited() throws Exception {
        var filter = new AuthRateLimitFilter(props(1), new ObjectMapper().findAndRegisterModules());
        for (int i = 0; i < 5; i++) {
            assertThat(call(filter, "GET", "/api/services", "10.0.0.3").getStatus()).isEqualTo(200);
            assertThat(call(filter, "POST", "/api/auth/logout", "10.0.0.3").getStatus()).isEqualTo(200);
        }
        assertThat(call(filter, "POST", "/api/auth/register/customer", "10.0.0.3").getStatus()).isEqualTo(200);
        assertThat(call(filter, "POST", "/api/auth/register/provider", "10.0.0.3").getStatus()).isEqualTo(429);
    }

    @Test
    void disabledLimiterLetsEverythingThrough() throws Exception {
        Rule rule = new Rule(1, Duration.ofMinutes(1));
        var filter = new AuthRateLimitFilter(new RateLimitProperties(false, rule, rule, rule, rule),
                new ObjectMapper());
        for (int i = 0; i < 3; i++) {
            assertThat(call(filter, "POST", "/api/auth/login", "10.0.0.4").getStatus()).isEqualTo(200);
        }
    }

    @Test
    void accountIsTemporarilyBlockedAfterTooManyFailuresAndSuccessResets() {
        var throttle = new BucketLoginThrottle(props(3));
        for (int i = 0; i < 3; i++) {
            assertThatCode(() -> throttle.checkAllowed("victime@mail.com")).doesNotThrowAnyException();
            throttle.recordFailure("victime@mail.com");
        }
        assertThatThrownBy(() -> throttle.checkAllowed("victime@mail.com"))
                .extracting("type").isEqualTo(ErrorType.TOO_MANY_REQUESTS);
        assertThatCode(() -> throttle.checkAllowed("autre@mail.com")).doesNotThrowAnyException();

        throttle.recordSuccess("victime@mail.com");
        assertThatCode(() -> throttle.checkAllowed("victime@mail.com")).doesNotThrowAnyException();
    }
}
