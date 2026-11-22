package com.testhakni.backend.messaging.infrastructure.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

import com.testhakni.backend.identity.domain.model.AccountStatus;
import com.testhakni.backend.identity.domain.model.Role;
import com.testhakni.backend.identity.domain.model.User;
import com.testhakni.backend.identity.domain.repository.UserRepository;
import com.testhakni.backend.identity.infrastructure.security.JwtProperties;
import com.testhakni.backend.identity.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

/** SEC-09 : seul un compte actif authentifie se connecte, et il n'ecoute que sa propre file. */
class StompAuthInterceptorTest {

    private static final String SECRET =
            Base64.getEncoder().encodeToString("unit-test-secret-unit-test-secret-1234".getBytes());

    private final JwtService jwt = new JwtService(JwtProperties.of(SECRET, Duration.ofMinutes(5), Duration.ofHours(1)));
    private final UserRepository users = mock(UserRepository.class);
    private final StompAuthInterceptor interceptor = new StompAuthInterceptor(jwt, users);
    private final User alice = User.builder().id("u1").email("a@mail.com").role(Role.CUSTOMER).build();

    private static Message<byte[]> frame(StompCommand command, String authorization, String destination, Principal user) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        if (authorization != null) accessor.setNativeHeader("Authorization", authorization);
        if (destination != null) accessor.setDestination(destination);
        if (user != null) accessor.setUser(user);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    @Test
    void connectWithValidTokenSetsTheAccountAsPrincipal() {
        when(users.findById("u1")).thenReturn(Optional.of(alice));
        Message<?> result = interceptor.preSend(
                frame(StompCommand.CONNECT, "Bearer " + jwt.generateAccessToken(alice), null, null), null);
        assertThat(StompHeaderAccessor.wrap(result).getUser().getName()).isEqualTo("u1");
    }

    @Test
    void connectWithoutTokenOrForSuspendedAccountIsRefused() {
        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.CONNECT, null, null, null), null))
                .isInstanceOf(MessagingException.class);
        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.CONNECT, "Bearer faux.jeton", null, null), null))
                .isInstanceOf(MessagingException.class);

        User suspended = User.builder().id("u2").role(Role.CUSTOMER).status(AccountStatus.SUSPENDED).build();
        when(users.findById("u2")).thenReturn(Optional.of(suspended));
        assertThatThrownBy(() -> interceptor.preSend(
                frame(StompCommand.CONNECT, "Bearer " + jwt.generateAccessToken(suspended), null, null), null))
                .isInstanceOf(MessagingException.class);
    }

    @Test
    void onlyOwnQueueCanBeSubscribedAndClientSendIsRefused() {
        Principal me = () -> "u1";
        assertThat(interceptor.preSend(frame(StompCommand.SUBSCRIBE, null, "/user/queue/messages", me), null)).isNotNull();
        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.SUBSCRIBE, null, "/queue/messages-u2", me), null))
                .isInstanceOf(MessagingException.class);
        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.SUBSCRIBE, null, "/user/queue/messages", null), null))
                .isInstanceOf(MessagingException.class);
        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.SEND, null, "/app/chat", me), null))
                .isInstanceOf(MessagingException.class);
    }
}
