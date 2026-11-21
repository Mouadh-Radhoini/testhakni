package com.testhakni.backend.identity.infrastructure.persistence;

import java.time.Instant;
import java.util.Optional;

import com.testhakni.backend.identity.domain.model.RefreshToken;
import com.testhakni.backend.identity.domain.repository.RefreshTokenRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/** Adaptateur MongoDB du port {@link RefreshTokenRepository}. */
@Repository
class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final MongoTemplate mongo;

    RefreshTokenRepositoryAdapter(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        return mongo.save(token);
    }

    @Override
    public Optional<RefreshToken> findById(String id) {
        return Optional.ofNullable(mongo.findById(id, RefreshToken.class));
    }

    @Override
    public boolean consume(String id, Instant now) {
        var active = Criteria.where("_id").is(id).and("revokedAt").is(null).and("expiresAt").gt(now);
        return mongo.updateFirst(Query.query(active), Update.update("revokedAt", now), RefreshToken.class)
                .getModifiedCount() == 1;
    }

    @Override
    public void revokeFamily(String familyId, Instant now) {
        revokeWhere(Criteria.where("familyId").is(familyId), now);
    }

    @Override
    public void revokeAllForUser(String userId, Instant now) {
        revokeWhere(Criteria.where("userId").is(userId), now);
    }

    private void revokeWhere(Criteria criteria, Instant now) {
        mongo.updateMulti(Query.query(criteria.and("revokedAt").is(null)), Update.update("revokedAt", now),
                RefreshToken.class);
    }
}
