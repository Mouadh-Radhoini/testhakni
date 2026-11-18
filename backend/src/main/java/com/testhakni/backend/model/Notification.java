package com.testhakni.backend.model;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("notifications")
public record Notification(
        @Id String id,
        @Indexed String recipientId,
        String type,
        String referenceId,
        String message,
        Instant createdAt,
        Instant readAt) {
}
