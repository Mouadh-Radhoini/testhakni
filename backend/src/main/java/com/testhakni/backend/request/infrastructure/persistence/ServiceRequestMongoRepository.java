package com.testhakni.backend.request.infrastructure.persistence;

import java.util.List;

import com.testhakni.backend.request.domain.model.RequestStatus;
import com.testhakni.backend.request.domain.model.ServiceRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

/** Spring Data : detail technique, utilise uniquement par l'adaptateur. */
interface ServiceRequestMongoRepository extends MongoRepository<ServiceRequest, String> {

    List<ServiceRequest> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    List<ServiceRequest> findByCustomerIdAndStatusOrderByCreatedAtDesc(String customerId, RequestStatus status);
}
