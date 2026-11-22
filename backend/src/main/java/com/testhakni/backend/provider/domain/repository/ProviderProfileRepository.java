package com.testhakni.backend.provider.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.testhakni.backend.provider.domain.model.ProviderProfile;

/** Port du domaine : profils professionnels des prestataires. */
public interface ProviderProfileRepository {

    Optional<ProviderProfile> findByUserId(String userId);

    List<ProviderProfile> findByUserIdIn(Collection<String> userIds);

    ProviderProfile save(ProviderProfile profile);

    List<ProviderProfile> saveAll(List<ProviderProfile> profiles);
}
