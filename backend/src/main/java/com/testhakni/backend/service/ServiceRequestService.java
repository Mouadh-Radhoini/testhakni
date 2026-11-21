package com.testhakni.backend.service;

import java.util.List;

import com.testhakni.backend.dto.RequestPayload;
import com.testhakni.backend.dto.RequestResponse;
import com.testhakni.backend.exception.ApiException;
import com.testhakni.backend.model.RequestStatus;
import com.testhakni.backend.model.ServiceOffering;
import com.testhakni.backend.model.ServiceRequest;
import com.testhakni.backend.model.ServiceStatus;
import com.testhakni.backend.repository.ServiceOfferingRepository;
import com.testhakni.backend.repository.ServiceRequestRepository;
import org.springframework.stereotype.Service;

/** Demandes de service des clients. REQ-01 / REQ-02 */
@Service
public class ServiceRequestService {

    private final ServiceRequestRepository requestRepository;
    private final ServiceOfferingRepository serviceRepository;

    private final OfferService offers;

    public ServiceRequestService(ServiceRequestRepository requestRepository,
                                 ServiceOfferingRepository serviceRepository, OfferService offers) {
        this.offers = offers;
        this.requestRepository = requestRepository;
        this.serviceRepository = serviceRepository;
    }

    public RequestResponse create(String customerId, RequestPayload p) {
        String serviceId = AuthService.trimToNull(p.serviceId());
        if (serviceId != null) {
            ServiceOffering service = serviceRepository.findById(serviceId)
                    .filter(s -> s.getStatus() == ServiceStatus.ACTIVE)
                    .orElseThrow(() -> ApiException.badRequest("Le service indique n'existe pas ou n'est plus disponible"));
            serviceId = service.getId();
        }
        ServiceRequest request = ServiceRequest.builder()
                .customerId(customerId)
                .serviceId(serviceId)
                .title(p.title().trim())
                .description(p.description().trim())
                .category(p.category())
                .city(p.city().trim())
                .address(AuthService.trimToNull(p.address()))
                .preferredDate(p.preferredDate())
                .budget(p.budget())
                .status(RequestStatus.OPEN)
                .build();
        return RequestResponse.from(requestRepository.save(request));
    }

    /** Uniquement les demandes du client connecte, filtrables par statut. */
    public List<RequestResponse> listMine(String customerId, RequestStatus status) {
        List<ServiceRequest> list = status == null
                ? requestRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                : requestRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, status);
        return list.stream().map(RequestResponse::from).toList();
    }

    public RequestResponse getMine(String customerId, String requestId) {
        return RequestResponse.from(loadOwned(customerId, requestId));
    }

    /** Annulation possible tant que la demande est OPEN. */
    @org.springframework.transaction.annotation.Transactional
    public RequestResponse cancel(String customerId, String requestId) {
        ServiceRequest request = loadOwned(customerId, requestId);
        if (request.getStatus() != RequestStatus.OPEN) {
            throw ApiException.conflict("Seule une demande ouverte peut etre annulee");
        }
        request.setStatus(RequestStatus.CANCELLED);
        offers.closeRemaining(requestId, com.testhakni.backend.model.OfferStatus.CANCELLED);
        return RequestResponse.from(requestRepository.save(request));
    }

    /** SEC-02 : 404 si inexistante, 403 si elle appartient a un autre client. */
    ServiceRequest loadOwned(String customerId, String requestId) {
        ServiceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> ApiException.notFound("Demande introuvable"));
        if (!request.getCustomerId().equals(customerId)) {
            throw ApiException.forbidden("Cette demande ne vous appartient pas");
        }
        return request;
    }
}
