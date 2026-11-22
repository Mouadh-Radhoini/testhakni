package com.testhakni.backend.contact.infrastructure;

import java.util.List;
import java.util.Optional;

import com.testhakni.backend.contact.domain.model.Contact;
import com.testhakni.backend.contact.domain.model.ContactStatus;
import com.testhakni.backend.contact.domain.repository.ContactRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

/** Adaptateur MongoDB du port {@link ContactRepository}. */
@Repository
class ContactRepositoryAdapter implements ContactRepository {

    private final MongoTemplate mongo;

    ContactRepositoryAdapter(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @Override
    public Optional<Contact> findById(String id) {
        return Optional.ofNullable(mongo.findById(id, Contact.class));
    }

    @Override
    public Optional<Contact> findByPair(String userA, String userB) {
        return Optional.ofNullable(mongo.findOne(
                Query.query(Criteria.where("pairKey").is(Contact.pairKey(userA, userB))), Contact.class));
    }

    @Override
    public Contact save(Contact contact) {
        return mongo.save(contact);
    }

    @Override
    public void delete(Contact contact) {
        mongo.remove(contact);
    }

    @Override
    public List<Contact> findByUser(String userId) {
        Query query = Query.query(new Criteria().orOperator(
                        Criteria.where("requesterId").is(userId), Criteria.where("targetId").is(userId)))
                .with(Sort.by(Sort.Direction.DESC, "createdAt"));
        return mongo.find(query, Contact.class);
    }

    @Override
    public long countIncomingPending(String userId) {
        return mongo.count(Query.query(Criteria.where("targetId").is(userId).and("status").is(ContactStatus.PENDING)),
                Contact.class);
    }
}
