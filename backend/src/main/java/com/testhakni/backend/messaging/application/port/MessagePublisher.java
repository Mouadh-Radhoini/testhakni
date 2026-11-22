package com.testhakni.backend.messaging.application.port;

import java.util.Collection;

import com.testhakni.backend.messaging.application.dto.MessagingViews.MessageView;

/** Port de sortie : pousse un nouveau message aux participants connectes (WebSocket). */
public interface MessagePublisher {

    void deliver(Collection<String> recipientIds, MessageView message);
}
