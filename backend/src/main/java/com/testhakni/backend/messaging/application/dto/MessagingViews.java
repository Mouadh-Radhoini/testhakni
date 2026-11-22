package com.testhakni.backend.messaging.application.dto;

import java.time.Instant;

import com.testhakni.backend.messaging.domain.model.Message;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Objets d'entree / sortie de la messagerie. */
public final class MessagingViews {
    private MessagingViews() {
    }

    /** Un fil vu par l'un de ses participants. */
    public record ConversationView(
            String id,
            String requestId,
            String title,
            String otherParticipantId,
            String otherParticipantName,
            String otherParticipantAvatarUrl,
            String lastMessagePreview,
            Instant lastMessageAt,
            long unread) {
    }

    public record MessageView(String id, String conversationId, String senderId, String body, Instant sentAt) {
        public static MessageView from(Message m) {
            return new MessageView(m.id(), m.conversationId(), m.senderId(), m.body(), m.sentAt());
        }
    }

    public record SendMessage(
            @NotBlank(message = "Le message est vide")
            @Size(max = Message.MAX_LENGTH, message = "2000 caracteres maximum") String body) {
    }

    public record UnreadCount(long unread) {
    }
}
