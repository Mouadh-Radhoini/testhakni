package com.testhakni.backend.messaging.domain.repository;

import java.time.Instant;
import java.util.List;

import com.testhakni.backend.messaging.domain.model.Message;

/** Port du domaine : messages. */
public interface MessageRepository {

    Message insert(Message message);

    /** Les {@code limit} derniers messages envoyes avant {@code before} (null = maintenant), du plus ancien au plus recent. */
    List<Message> findLatest(String conversationId, Instant before, int limit);

    /** Messages recus par {@code readerId} apres {@code after} (null = depuis le debut). */
    long countUnread(String conversationId, String readerId, Instant after);
}
