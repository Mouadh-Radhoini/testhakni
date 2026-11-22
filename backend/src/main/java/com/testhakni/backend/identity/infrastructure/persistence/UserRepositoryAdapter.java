package com.testhakni.backend.identity.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.testhakni.backend.identity.domain.model.AccountStatus;
import com.testhakni.backend.identity.domain.model.Role;
import com.testhakni.backend.identity.domain.model.User;
import com.testhakni.backend.identity.domain.repository.UserRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/** Adaptateur MongoDB du port {@link UserRepository}. */
@Repository
class UserRepositoryAdapter implements UserRepository {

    private final UserMongoRepository repository;
    private final MongoTemplate mongo;

    UserRepositoryAdapter(UserMongoRepository repository, MongoTemplate mongo) {
        this.repository = repository;
        this.mongo = mongo;
    }

    @Override
    public Optional<User> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        return repository.save(user);
    }

    @Override
    public void delete(User user) {
        repository.delete(user);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public List<User> search(Role role, AccountStatus status) {
        Query query = new Query();
        if (role != null) query.addCriteria(Criteria.where("role").is(role));
        if (status != null) query.addCriteria(Criteria.where("status").is(status));
        return mongo.find(query, User.class);
    }

    @Override
    public void updateStatus(String id, AccountStatus status) {
        mongo.updateFirst(Query.query(Criteria.where("_id").is(id)),
                Update.update("status", status).currentDate("updatedAt"), User.class);
    }
}
