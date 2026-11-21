package com.testhakni.backend.request.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.testhakni.backend.catalog.domain.model.ServiceCategory;
import com.testhakni.backend.request.domain.model.RequestStatus;
import com.testhakni.backend.request.domain.model.ServiceRequest;

/** Port du domaine : demandes de service des clients. */
public interface ServiceRequestRepository {

    Optional<ServiceRequest> findById(String id);

    boolean existsById(String id);

    ServiceRequest save(ServiceRequest request);

    /** Du plus recent au plus ancien. */
    List<ServiceRequest> findByCustomerId(String customerId);

    /** Du plus recent au plus ancien. */
    List<ServiceRequest> findByCustomerIdAndStatus(String customerId, RequestStatus status);

    /** Demandes OPEN d'une des categories, dans la ville (insensible a la casse), plus recentes d'abord. */
    List<ServiceRequest> findOpenMatching(Collection<ServiceCategory> categories, String city);

    /**
     * Ecrit atomiquement sur la demande si elle est encore OPEN et compatible, afin que l'acceptation
     * et l'annulation concurrentes ne puissent pas manquer une offre tardive.
     * Vide si la demande n'existe pas ou n'est pas eligible.
     */
    Optional<ServiceRequest> touchIfOpenMatching(String id, Collection<ServiceCategory> categories, String city);

    /** Transition atomique de statut : true si la demande etait bien dans le statut attendu. */
    boolean changeStatus(String id, RequestStatus expected, RequestStatus target);

    long count();
}
