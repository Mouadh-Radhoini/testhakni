package com.testhakni.backend.identity.infrastructure.persistence;

import java.util.Optional;

import com.testhakni.backend.identity.domain.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

/** Spring Data : detail technique, utilise uniquement par l'adaptateur. */
interface UserMongoRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
