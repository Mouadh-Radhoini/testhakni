package com.testhakni.backend.repository;

import java.util.List;

import com.testhakni.backend.model.RequestStatus;
import com.testhakni.backend.model.ServiceRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ServiceRequestRepository extends MongoRepository<ServiceRequest, String> {

    List<ServiceRequest> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    List<ServiceRequest> findByCustomerIdAndStatusOrderByCreatedAtDesc(String customerId, RequestStatus status);
}
