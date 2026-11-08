package com.testhakni.backend.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.testhakni.backend.model.ProviderProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProviderProfileRepository extends MongoRepository<ProviderProfile, String> {

    Optional<ProviderProfile> findByUserId(String userId);

    boolean existsByUserId(String userId);

    List<ProviderProfile> findByUserIdIn(Collection<String> userIds);
}
