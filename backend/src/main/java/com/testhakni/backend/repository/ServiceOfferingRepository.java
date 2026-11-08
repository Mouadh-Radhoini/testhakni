package com.testhakni.backend.repository;

import java.util.List;

import com.testhakni.backend.model.ServiceOffering;
import com.testhakni.backend.model.ServiceStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

/** CRUD simple. La recherche multi-criteres est dans ServiceSearchService (MongoTemplate). */
public interface ServiceOfferingRepository extends MongoRepository<ServiceOffering, String> {

    List<ServiceOffering> findByProviderIdOrderByCreatedAtDesc(String providerId);

    List<ServiceOffering> findByProviderIdAndStatusOrderByCreatedAtDesc(String providerId, ServiceStatus status);
}
