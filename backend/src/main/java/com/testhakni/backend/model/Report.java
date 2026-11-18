package com.testhakni.backend.model;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("reports")
public record Report(
        @Id String id,
        String reporterId,
        ContentType targetType,
        String targetId,
        String reason,
        ReportStatus status,
        Instant createdAt,
        String moderatorId,
        String decision) {
}
