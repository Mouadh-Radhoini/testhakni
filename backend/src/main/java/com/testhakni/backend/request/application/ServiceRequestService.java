package com.testhakni.backend.request.application;

import java.util.List;

import com.testhakni.backend.catalog.domain.model.ServiceOffering;
import com.testhakni.backend.catalog.domain.model.ServiceStatus;
import com.testhakni.backend.catalog.domain.repository.ServiceOfferingRepository;
import com.testhakni.backend.offer.application.OfferService;
import com.testhakni.backend.offer.domain.model.OfferStatus;
import com.testhakni.backend.request.application.dto.RequestPayload;
import com.testhakni.backend.request.application.dto.RequestResponse;
import com.testhakni.backend.request.domain.model.RequestStatus;
import com.testhakni.backend.request.domain.model.ServiceRequest;
import com.testhakni.backend.request.domain.repository.ServiceRequestRepository;
import com.testhakni.backend.shared.domain.Text;
import com.testhakni.backend.shared.domain.exception.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cas d'usage : demandes de service des clients. REQ-01 / REQ-02 */
@Service
public class ServiceRequestService {

    private final ServiceRequestRepository requestRepository;
    private final ServiceOfferingRepository serviceRepository;
    private final OfferService offers;

    public ServiceRequestService(ServiceRequestRepository requestRepository,
                                 ServiceOfferingRepository serviceRepository, OfferService offers) {
        this.requestRepository = requestRepository;
        this.serviceRepository = serviceRepository;
        this.offers = offers;
    }

    public RequestResponse create(String customerId, RequestPayload p) {
        String serviceId = Text.trimToNull(p.serviceId());
        if (serviceId != null) {
            ServiceOffering service = serviceRepository.findById(serviceId)
                    .filter(s -> s.getStatus() == ServiceStatus.ACTIVE)
                    .orElseThrow(() -> DomainException.badRequest("Le service indique n'existe pas ou n'est plus disponible"));
            serviceId = service.getId();
        }
        ServiceRequest request = ServiceRequest.builder()
                .customerId(customerId)
                .serviceId(serviceId)
                .title(p.title().trim())
                .description(p.description().trim())
                .category(p.category())
                .city(p.city().trim())
                .address(Text.trimToNull(p.address()))
                .preferredDate(p.preferredDate())
                .budget(p.budget())
                .status(RequestStatus.OPEN)
                .build();
        return RequestResponse.from(requestRepository.save(request));
    }

    /** Uniquement les demandes du client connecte, filtrables par statut. */
    public List<RequestResponse> listMine(String customerId, RequestStatus status) {
        List<ServiceRequest> list = status == null
                ? requestRepository.findByCustomerId(customerId)
                : requestRepository.findByCustomerIdAndStatus(customerId, status);
        return list.stream().map(RequestResponse::from).toList();
    }

    public RequestResponse getMine(String customerId, String requestId) {
        return RequestResponse.from(loadOwned(customerId, requestId));
    }

    /** Annulation possible tant que la demande est OPEN. */
    @Transactional
    public RequestResponse cancel(String customerId, String requestId) {
        ServiceRequest request = loadOwned(customerId, requestId);
        if (request.getStatus() != RequestStatus.OPEN) {
            throw DomainException.conflict("Seule une demande ouverte peut etre annulee");
        }
        request.setStatus(RequestStatus.CANCELLED);
        offers.closeRemaining(requestId, OfferStatus.CANCELLED);
        return RequestResponse.from(requestRepository.save(request));
    }

    /** SEC-02 : 404 si inexistante, 403 si elle appartient a un autre client. */
    ServiceRequest loadOwned(String customerId, String requestId) {
        ServiceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> DomainException.notFound("Demande introuvable"));
        if (!request.getCustomerId().equals(customerId)) {
            throw DomainException.forbidden("Cette demande ne vous appartient pas");
        }
        return request;
    }
}
