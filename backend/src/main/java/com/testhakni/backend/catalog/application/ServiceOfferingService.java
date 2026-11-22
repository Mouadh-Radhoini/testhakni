package com.testhakni.backend.catalog.application;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.testhakni.backend.catalog.application.dto.ServicePayload;
import com.testhakni.backend.catalog.application.dto.ServiceResponse;
import com.testhakni.backend.catalog.domain.model.PriceType;
import com.testhakni.backend.catalog.domain.model.ServiceOffering;
import com.testhakni.backend.catalog.domain.model.ServiceStatus;
import com.testhakni.backend.catalog.domain.repository.ServiceOfferingRepository;
import com.testhakni.backend.catalog.domain.repository.ServiceOfferingRepository.SearchCriteria;
import com.testhakni.backend.catalog.domain.repository.ServiceOfferingRepository.SortOption;
import com.testhakni.backend.identity.domain.model.AuthenticatedUser;
import com.testhakni.backend.provider.application.dto.ProviderSummary;
import com.testhakni.backend.provider.domain.repository.ProviderProfileRepository;
import com.testhakni.backend.shared.application.PageResponse;
import com.testhakni.backend.shared.domain.exception.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cas d'usage : gestion des services (SERV-01 a SERV-04) et recherche publique (SEARCH-01 a SEARCH-03). */
@Service
public class ServiceOfferingService {

    private final ServiceOfferingRepository serviceRepository;
    private final ProviderProfileRepository profileRepository;

    public ServiceOfferingService(ServiceOfferingRepository serviceRepository,
                                  ProviderProfileRepository profileRepository) {
        this.serviceRepository = serviceRepository;
        this.profileRepository = profileRepository;
    }

    // ---------------------------------------------------------------- prestataire

    public ServiceResponse create(String providerId, ServicePayload payload) {
        ServiceOffering service = ServiceOffering.builder()
                .providerId(providerId)
                .status(ServiceStatus.ACTIVE)
                .build();
        apply(service, payload);
        return toResponse(serviceRepository.save(service));
    }

    @Transactional
    public ServiceResponse update(String providerId, String serviceId, ServicePayload payload) {
        ServiceOffering service = loadOwned(providerId, serviceId);
        apply(service, payload);
        return toResponse(serviceRepository.save(service));
    }

    /** Le prestataire peut activer / desactiver son service, mais pas lever une suspension admin. */
    @Transactional
    public ServiceResponse changeStatus(String providerId, String serviceId, ServiceStatus status) {
        if (status == ServiceStatus.SUSPENDED) {
            throw DomainException.badRequest("Statut autorise : ACTIVE ou INACTIVE");
        }
        ServiceOffering service = loadOwned(providerId, serviceId);
        if (service.getStatus() == ServiceStatus.SUSPENDED) {
            throw DomainException.forbidden("Service suspendu par un administrateur");
        }
        service.setStatus(status);
        return toResponse(serviceRepository.save(service));
    }

    public void delete(String providerId, String serviceId) {
        serviceRepository.delete(loadOwned(providerId, serviceId));
    }

    public List<ServiceResponse> listMine(String providerId) {
        return toResponses(serviceRepository.findByProviderId(providerId));
    }

    // ---------------------------------------------------------------- public

    /**
     * Detail d'un service. Visible par tous s'il est ACTIVE ; sinon seulement par son proprietaire.
     * Un visiteur sans compte n'en voit qu'un apercu : prix, description et prestataire sont reserves aux inscrits.
     */
    public ServiceResponse getById(String serviceId, AuthenticatedUser viewer) {
        ServiceOffering service = serviceRepository.findById(serviceId)
                .filter(s -> s.getStatus() == ServiceStatus.ACTIVE
                        || (viewer != null && viewer.id().equals(s.getProviderId())))
                .orElseThrow(() -> DomainException.notFound("Service introuvable"));
        return viewer == null ? ServiceResponse.teaser(service) : toResponse(service);
    }

    /** Verifie seulement que le service est visible publiquement (404 sinon). */
    public void requireVisible(String serviceId) {
        serviceRepository.findById(serviceId)
                .filter(s -> s.getStatus() == ServiceStatus.ACTIVE)
                .orElseThrow(() -> DomainException.notFound("Service introuvable"));
    }

    /**
     * Recherche paginee parmi les services ACTIVE uniquement.
     * Les prix etant reserves aux inscrits, filtrer ou trier par prix exige d'etre connecte.
     */
    public PageResponse<ServiceResponse> search(SearchCriteria c, int page, int size, SortOption sort,
                                                AuthenticatedUser viewer) {
        boolean usesPrice = c.minPrice() != null || c.maxPrice() != null || sort != SortOption.NEWEST;
        if (viewer == null && usesPrice) {
            throw DomainException.unauthorized("Connectez-vous pour filtrer ou trier par prix");
        }
        if (c.minPrice() != null && c.maxPrice() != null && c.minPrice().compareTo(c.maxPrice()) > 0) {
            throw DomainException.badRequest("Le prix minimum doit etre inferieur ou egal au prix maximum");
        }
        PageResponse<ServiceOffering> result = serviceRepository.searchActive(c, page, size, sort);
        List<ServiceResponse> content = viewer == null
                ? result.content().stream().map(ServiceResponse::teaser).toList()
                : toResponses(result.content());
        return new PageResponse<>(content, result.page(), result.size(), result.totalElements(),
                result.totalPages(), result.first(), result.last());
    }

    /** Vue d'administration : tous les services, sans resume prestataire. */
    public List<ServiceResponse> listAll() {
        return serviceRepository.findAll().stream().map(s -> ServiceResponse.from(s, null)).toList();
    }

    // ---------------------------------------------------------------- utilitaires

    /** SEC-02 : 404 si le service n'existe pas, 403 s'il appartient a un autre prestataire. */
    ServiceOffering loadOwned(String providerId, String serviceId) {
        ServiceOffering service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> DomainException.notFound("Service introuvable"));
        if (!service.getProviderId().equals(providerId)) {
            throw DomainException.forbidden("Ce service ne vous appartient pas");
        }
        return service;
    }

    private void apply(ServiceOffering s, ServicePayload p) {
        s.setTitle(p.title().trim());
        s.setDescription(p.description().trim());
        s.setCategory(p.category());
        s.setCity(p.city().trim());
        s.setPriceType(p.priceType());
        s.setPrice(p.priceType() == PriceType.ON_QUOTE ? null : p.price());
    }

    private ServiceResponse toResponse(ServiceOffering s) {
        ProviderSummary provider = profileRepository.findByUserId(s.getProviderId())
                .map(ProviderSummary::from).orElse(null);
        return ServiceResponse.from(s, provider);
    }

    /** Charge les profils prestataires en une seule requete (evite N+1). */
    private List<ServiceResponse> toResponses(List<ServiceOffering> services) {
        Set<String> providerIds = services.stream().map(ServiceOffering::getProviderId).collect(Collectors.toSet());
        Map<String, ProviderSummary> summaries = profileRepository.findByUserIdIn(providerIds).stream()
                .map(ProviderSummary::from)
                .collect(Collectors.toMap(ProviderSummary::providerId, Function.identity(), (a, b) -> a));
        return services.stream().map(s -> ServiceResponse.from(s, summaries.get(s.getProviderId()))).toList();
    }
}
