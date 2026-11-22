package com.testhakni.backend.messaging.infrastructure.persistence;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.testhakni.backend.messaging.domain.model.Conversation;
import com.testhakni.backend.messaging.domain.model.Message;
import com.testhakni.backend.messaging.domain.repository.ConversationRepository;
import com.testhakni.backend.messaging.domain.repository.MessageRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/** Adaptateur MongoDB des ports de messagerie. */
@Repository
class MessagingRepositoryAdapter implements ConversationRepository, MessageRepository {

    private final MongoTemplate mongo;

    MessagingRepositoryAdapter(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    // ------------------------------------------------------------ conversations

    @Override
    public Optional<Conversation> findById(String id) {
        return Optional.ofNullable(mongo.findById(id, Conversation.class));
    }

    @Override
    public Optional<Conversation> findByRequestAndProvider(String requestId, String providerId) {
        return Optional.ofNullable(mongo.findOne(
                Query.query(Criteria.where("requestId").is(requestId).and("providerId").is(providerId)),
                Conversation.class));
    }

    @Override
    public Conversation openIfAbsent(Conversation conversation) {
        var existing = findByRequestAndProvider(conversation.requestId(), conversation.providerId());
        if (existing.isPresent()) return existing.get();
        try {
            return mongo.insert(conversation);
        } catch (DuplicateKeyException race) {
            // Deux ouvertures simultanees : l'index unique garantit un seul fil, on renvoie le gagnant
            return findByRequestAndProvider(conversation.requestId(), conversation.providerId()).orElseThrow();
        }
    }

    @Override
    public List<Conversation> findByParticipant(String userId) {
        Query query = Query.query(new Criteria().orOperator(
                        Criteria.where("customerId").is(userId), Criteria.where("providerId").is(userId)))
                .with(Sort.by(Sort.Order.desc("lastMessageAt"), Sort.Order.desc("createdAt")));
        return mongo.find(query, Conversation.class);
    }

    @Override
    public void recordMessage(String id, String preview, Instant at) {
        mongo.updateFirst(Query.query(Criteria.where("_id").is(id)),
                Update.update("lastMessageAt", at).set("lastMessagePreview", preview), Conversation.class);
    }

    @Override
    public void markRead(Conversation conversation, String userId, Instant at) {
        String field = conversation.customerId().equals(userId) ? "customerReadAt" : "providerReadAt";
        mongo.updateFirst(Query.query(Criteria.where("_id").is(conversation.id())), Update.update(field, at),
                Conversation.class);
    }

    // ------------------------------------------------------------ messages

    @Override
    public Message insert(Message message) {
        return mongo.insert(message);
    }

    @Override
    public List<Message> findLatest(String conversationId, Instant before, int limit) {
        Criteria criteria = Criteria.where("conversationId").is(conversationId);
        if (before != null) criteria = criteria.and("sentAt").lt(before);
        List<Message> newestFirst = mongo.find(Query.query(criteria)
                .with(Sort.by(Sort.Direction.DESC, "sentAt")).limit(limit), Message.class);
        List<Message> chronological = new ArrayList<>(newestFirst);
        Collections.reverse(chronological);
        return chronological;
    }

    @Override
    public long countUnread(String conversationId, String readerId, Instant after) {
        Criteria criteria = Criteria.where("conversationId").is(conversationId).and("senderId").ne(readerId);
        if (after != null) criteria = criteria.and("sentAt").gt(after);
        return mongo.count(Query.query(criteria), Message.class);
    }
}
