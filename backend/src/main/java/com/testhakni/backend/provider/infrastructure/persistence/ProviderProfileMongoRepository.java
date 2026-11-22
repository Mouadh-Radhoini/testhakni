package com.testhakni.backend.provider.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.testhakni.backend.provider.domain.model.ProviderProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

/** Spring Data : detail technique, utilise uniquement par l'adaptateur. */
interface ProviderProfileMongoRepository extends MongoRepository<ProviderProfile, String> {

    Optional<ProviderProfile> findByUserId(String userId);

    List<ProviderProfile> findByUserIdIn(Collection<String> userIds);
}
