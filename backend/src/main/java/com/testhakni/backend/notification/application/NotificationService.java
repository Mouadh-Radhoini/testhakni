package com.testhakni.backend.notification.application;

import java.time.Instant;

import com.testhakni.backend.notification.domain.model.Notification;
import com.testhakni.backend.notification.domain.repository.NotificationRepository;
import com.testhakni.backend.shared.application.PageResponse;
import com.testhakni.backend.shared.domain.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Cas d'usage : envoi et lecture des notifications in-app. */
@Service
@RequiredArgsConstructor
public class NotificationService {
    static final int PAGE_SIZE = 30;

    private final NotificationRepository notifications;

    public void send(String user, String type, String ref, String message) {
        notifications.insert(new Notification(null, user, type, ref, message, Instant.now(), null));
    }

    public PageResponse<Notification> list(String user, int page) {
        return notifications.findByRecipient(user, page, PAGE_SIZE);
    }

    public long unread(String user) {
        return notifications.countUnread(user);
    }

    public void read(String user, String id) {
        if (!notifications.existsForRecipient(id, user)) {
            throw DomainException.notFound("Notification introuvable");
        }
        notifications.markRead(id, user);
    }
}
