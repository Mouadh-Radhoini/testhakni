package com.testhakni.backend.messaging.infrastructure.websocket;

import java.util.Collection;

import com.testhakni.backend.messaging.application.dto.MessagingViews.MessageView;
import com.testhakni.backend.messaging.application.port.MessagePublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Adaptateur STOMP du port {@link MessagePublisher} : envoi sur /user/{id}/queue/messages. */
@Component
class StompMessagePublisher implements MessagePublisher {

    private final SimpMessagingTemplate template;

    StompMessagePublisher(SimpMessagingTemplate template) {
        this.template = template;
    }

    @Override
    public void deliver(Collection<String> recipientIds, MessageView message) {
        recipientIds.forEach(id -> template.convertAndSendToUser(id, "/queue/messages", message));
    }
}
