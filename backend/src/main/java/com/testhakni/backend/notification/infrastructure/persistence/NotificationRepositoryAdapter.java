package com.testhakni.backend.notification.infrastructure.persistence;

import java.time.Instant;

import com.testhakni.backend.notification.domain.model.Notification;
import com.testhakni.backend.notification.domain.repository.NotificationRepository;
import com.testhakni.backend.shared.application.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/** Adaptateur MongoDB du port {@link NotificationRepository}. */
@Repository
class NotificationRepositoryAdapter implements NotificationRepository {

    private final NotificationMongoRepository repository;
    private final MongoTemplate mongo;

    NotificationRepositoryAdapter(NotificationMongoRepository repository, MongoTemplate mongo) {
        this.repository = repository;
        this.mongo = mongo;
    }

    @Override
    public Notification insert(Notification notification) {
        return repository.insert(notification);
    }

    @Override
    public PageResponse<Notification> findByRecipient(String recipientId, int page, int size) {
        Page<Notification> result = repository.findByRecipientIdOrderByCreatedAtDesc(recipientId, PageRequest.of(page, size));
        return new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages(), result.isFirst(), result.isLast());
    }

    @Override
    public long countUnread(String recipientId) {
        return repository.countByRecipientIdAndReadAtNull(recipientId);
    }

    @Override
    public boolean existsForRecipient(String id, String recipientId) {
        return mongo.exists(Query.query(owned(id, recipientId)), Notification.class);
    }

    @Override
    public void markRead(String id, String recipientId) {
        mongo.updateFirst(Query.query(owned(id, recipientId).and("readAt").is(null)),
                Update.update("readAt", Instant.now()), Notification.class);
    }

    private static Criteria owned(String id, String recipientId) {
        return Criteria.where("_id").is(id).and("recipientId").is(recipientId);
    }
}
