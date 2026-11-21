package com.testhakni.backend.model;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("moderation_actions")
public record ModerationAction(@Id String id, String moderatorId, String targetType, String targetId,
                               String decision, String reason, Instant at) {}
