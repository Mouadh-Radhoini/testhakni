package com.testhakni.backend.notification.infrastructure.persistence;

import com.testhakni.backend.notification.domain.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/** Spring Data : detail technique, utilise uniquement par l'adaptateur. */
interface NotificationMongoRepository extends MongoRepository<Notification, String> {

    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId, Pageable page);

    long countByRecipientIdAndReadAtNull(String recipientId);
}
