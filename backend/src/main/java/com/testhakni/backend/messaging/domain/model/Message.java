package com.testhakni.backend.messaging.domain.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/** Message d'un fil de discussion. Texte brut, 2000 caracteres maximum. */
@Document("messages")
@CompoundIndex(name = "conversation_sent", def = "{'conversationId': 1, 'sentAt': -1}")
public record Message(
        @Id String id,
        String conversationId,
        String senderId,
        String body,
        Instant sentAt) {

    public static final int MAX_LENGTH = 2000;

    /** Apercu court affiche dans la liste des conversations. */
    public String preview() {
        return body.length() <= 80 ? body : body.substring(0, 77) + "…";
    }
}
