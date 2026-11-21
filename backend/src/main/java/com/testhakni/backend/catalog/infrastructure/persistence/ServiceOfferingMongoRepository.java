package com.testhakni.backend.catalog.infrastructure.persistence;

import java.util.List;

import com.testhakni.backend.catalog.domain.model.ServiceOffering;
import com.testhakni.backend.catalog.domain.model.ServiceStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

/** Spring Data : detail technique, utilise uniquement par l'adaptateur. */
interface ServiceOfferingMongoRepository extends MongoRepository<ServiceOffering, String> {

    List<ServiceOffering> findByProviderIdOrderByCreatedAtDesc(String providerId);

    List<ServiceOffering> findByProviderIdAndStatusOrderByCreatedAtDesc(String providerId, ServiceStatus status);
}
