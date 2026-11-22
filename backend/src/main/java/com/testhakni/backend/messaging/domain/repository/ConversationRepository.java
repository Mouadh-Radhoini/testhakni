package com.testhakni.backend.messaging.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.testhakni.backend.messaging.domain.model.Conversation;

/** Port du domaine : fils de discussion. */
public interface ConversationRepository {

    Optional<Conversation> findById(String id);

    Optional<Conversation> findByRequestAndProvider(String requestId, String providerId);

    /** Fil direct (sans demande) entre ce client et ce prestataire. */
    Optional<Conversation> findDirect(String customerId, String providerId);

    /** Cree le fil, ou renvoie celui qui existe deja pour ce triplet demande / client / prestataire. */
    Conversation openIfAbsent(Conversation conversation);

    /** Fils d'un participant, du plus recemment actif au moins actif. */
    List<Conversation> findByParticipant(String userId);

    void recordMessage(String id, String preview, Instant at);

    void markRead(Conversation conversation, String userId, Instant at);
}
