package com.testhakni.backend.provider.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.testhakni.backend.provider.domain.model.ProviderProfile;
import com.testhakni.backend.provider.domain.repository.ProviderProfileRepository;
import org.springframework.stereotype.Repository;

/** Adaptateur MongoDB du port {@link ProviderProfileRepository}. */
@Repository
class ProviderProfileRepositoryAdapter implements ProviderProfileRepository {

    private final ProviderProfileMongoRepository repository;

    ProviderProfileRepositoryAdapter(ProviderProfileMongoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ProviderProfile> findByUserId(String userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public List<ProviderProfile> findByUserIdIn(Collection<String> userIds) {
        return repository.findByUserIdIn(userIds);
    }

    @Override
    public ProviderProfile save(ProviderProfile profile) {
        return repository.save(profile);
    }

    @Override
    public List<ProviderProfile> saveAll(List<ProviderProfile> profiles) {
        return repository.saveAll(profiles);
    }
}
