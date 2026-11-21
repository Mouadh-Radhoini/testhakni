package com.testhakni.backend.service;

import java.time.Instant;
import com.testhakni.backend.model.Notification;
import com.testhakni.backend.dto.PageResponse;
import com.testhakni.backend.exception.ApiException;
import com.testhakni.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notifications;
    private final MongoTemplate mongo;

    public void send(String user, String type, String ref, String message) {
        notifications.insert(new Notification(null, user, type, ref, message, Instant.now(), null));
    }

    public PageResponse<Notification> list(String user, int page) {
        return PageResponse.from(notifications.findByRecipientIdOrderByCreatedAtDesc(user,
                PageRequest.of(page, 30)), n -> n);
    }

    public long unread(String user) {
        return notifications.countByRecipientIdAndReadAtNull(user);
    }

    public void read(String user, String id) {
        var owned = Criteria.where("_id").is(id).and("recipientId").is(user);
        if (!mongo.exists(Query.query(owned), Notification.class)) {
            throw ApiException.notFound("Notification introuvable");
        }
        mongo.updateFirst(Query.query(owned.and("readAt").is(null)),
                Update.update("readAt", Instant.now()), Notification.class);
    }
}
