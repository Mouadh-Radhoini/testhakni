package com.testhakni.backend.repository;

import com.testhakni.backend.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId, Pageable page);
    long countByRecipientIdAndReadAtNull(String recipientId);
}
