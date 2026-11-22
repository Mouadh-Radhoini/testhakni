package com.testhakni.backend.messaging.infrastructure.websocket;

import java.security.Principal;

import com.testhakni.backend.identity.domain.model.User;
import com.testhakni.backend.identity.domain.repository.UserRepository;
import com.testhakni.backend.identity.infrastructure.security.JwtService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * Securite de la messagerie temps reel (SEC-09) :
 * - CONNECT exige "Authorization: Bearer <access token>" valide, compte actif ;
 * - SUBSCRIBE n'est permis que sur sa propre file /user/queue/messages ;
 * - SEND est refuse : on ecrit par l'API REST, qui valide et controle l'appartenance au fil.
 */
@Component
public class StompAuthInterceptor implements ChannelInterceptor {

    static final String OWN_QUEUE = "/user/queue/messages";

    private final JwtService jwtService;
    private final UserRepository users;

    public StompAuthInterceptor(JwtService jwtService, UserRepository users) {
        this.jwtService = jwtService;
        this.users = users;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) return message;

        switch (accessor.getCommand()) {
            case CONNECT -> accessor.setUser(authenticate(accessor.getFirstNativeHeader("Authorization")));
            case SUBSCRIBE -> {
                if (accessor.getUser() == null || !OWN_QUEUE.equals(accessor.getDestination())) {
                    throw new MessagingException("Abonnement refuse");
                }
            }
            case SEND -> throw new MessagingException("Envoyez les messages par l'API REST");
            default -> { /* DISCONNECT, heartbeats... */ }
        }
        return message;
    }

    private Principal authenticate(String header) {
        if (header == null || !header.startsWith("Bearer ")) throw new MessagingException("Authentification requise");
        String userId = jwtService.parse(header.substring(7), JwtService.TYPE_ACCESS)
                .flatMap(claims -> users.findById(claims.getSubject()))
                .filter(User::isActive)
                .map(User::getId)
                .orElseThrow(() -> new MessagingException("Token invalide ou expire"));
        // Le nom du Principal sert de cle aux destinations /user/... : c'est l'id du compte.
        return () -> userId;
    }
}
