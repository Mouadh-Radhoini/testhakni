package com.testhakni.backend.notification.domain.repository;

import com.testhakni.backend.notification.domain.model.Notification;
import com.testhakni.backend.shared.application.PageResponse;

/** Port du domaine : notifications in-app. */
public interface NotificationRepository {

    Notification insert(Notification notification);

    /** Notifications d'un destinataire, plus recentes d'abord. */
    PageResponse<Notification> findByRecipient(String recipientId, int page, int size);

    long countUnread(String recipientId);

    boolean existsForRecipient(String id, String recipientId);

    /** Marque lue si elle ne l'est pas deja (idempotent). */
    void markRead(String id, String recipientId);
}
