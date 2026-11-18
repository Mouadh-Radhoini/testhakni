package com.testhakni.backend.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.testhakni.backend.dto.PageResponse;
import com.testhakni.backend.dto.ProviderSummary;
import com.testhakni.backend.dto.ServicePayload;
import com.testhakni.backend.dto.ServiceResponse;
import com.testhakni.backend.exception.ApiException;
import com.testhakni.backend.model.PriceType;
import com.testhakni.backend.model.ServiceCategory;
import com.testhakni.backend.model.ServiceOffering;
import com.testhakni.backend.model.ServiceStatus;
import com.testhakni.backend.repository.ProviderProfileRepository;
import com.testhakni.backend.repository.ServiceOfferingRepository;
import com.testhakni.backend.security.AuthenticatedUser;
import org.bson.types.Decimal128;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

/** Gestion des services (SERV-01 a SERV-04) et recherche publique (SEARCH-01 a SEARCH-03). */
@Service
public class ServiceOfferingService {

    /** Tris acceptes par GET /api/services?sort=... */
    public enum SortOption {
        NEWEST(Sort.by(Sort.Direction.DESC, "createdAt")),
        PRICE_ASC(Sort.by(Sort.Direction.ASC, "price").and(Sort.by(Sort.Direction.DESC, "createdAt"))),
        PRICE_DESC(Sort.by(Sort.Direction.DESC, "price").and(Sort.by(Sort.Direction.DESC, "createdAt")));

        private final Sort sort;

        SortOption(Sort sort) {
            this.sort = sort;
        }
    }

    /** Filtres de recherche (tous optionnels). */
    public record SearchCriteria(String q, ServiceCategory category, String city,
                                 BigDecimal minPrice, BigDecimal maxPrice) {
    }

    private final ServiceOfferingRepository serviceRepository;
    private final ProviderProfileRepository profileRepository;
    private final MongoTemplate mongoTemplate;

    public ServiceOfferingService(ServiceOfferingRepository serviceRepository,
                                  ProviderProfileRepository profileRepository,
                                  MongoTemplate mongoTemplate) {
        this.serviceRepository = serviceRepository;
        this.profileRepository = profileRepository;
        this.mongoTemplate = mongoTemplate;
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

    @org.springframework.transaction.annotation.Transactional
    public ServiceResponse update(String providerId, String serviceId, ServicePayload payload) {
        ServiceOffering service = loadOwned(providerId, serviceId);
        apply(service, payload);
        return toResponse(serviceRepository.save(service));
    }

    /** Le prestataire peut activer / desactiver son service, mais pas lever une suspension admin. */
    @org.springframework.transaction.annotation.Transactional
    public ServiceResponse changeStatus(String providerId, String serviceId, ServiceStatus status) {
        if (status == ServiceStatus.SUSPENDED) {
            throw ApiException.badRequest("Statut autorise : ACTIVE ou INACTIVE");
        }
        ServiceOffering service = loadOwned(providerId, serviceId);
        if (service.getStatus() == ServiceStatus.SUSPENDED) {
            throw ApiException.forbidden("Service suspendu par un administrateur");
        }
        service.setStatus(status);
        return toResponse(serviceRepository.save(service));
    }

    public void delete(String providerId, String serviceId) {
        serviceRepository.delete(loadOwned(providerId, serviceId));
    }

    public List<ServiceResponse> listMine(String providerId) {
        return toResponses(serviceRepository.findByProviderIdOrderByCreatedAtDesc(providerId));
    }

    // ---------------------------------------------------------------- public

    /**
     * Detail d'un service. Visible par tous s'il est ACTIVE ; sinon seulement par son proprietaire.
     */
    public ServiceResponse getById(String serviceId, AuthenticatedUser viewer) {
        ServiceOffering service = serviceRepository.findById(serviceId)
                .filter(s -> s.getStatus() == ServiceStatus.ACTIVE
                        || (viewer != null && viewer.id().equals(s.getProviderId())))
                .orElseThrow(() -> ApiException.notFound("Service introuvable"));
        return toResponse(service);
    }

    /** Recherche paginee parmi les services ACTIVE uniquement. */
    public PageResponse<ServiceResponse> search(SearchCriteria c, int page, int size, SortOption sort) {
        if (c.minPrice() != null && c.maxPrice() != null && c.minPrice().compareTo(c.maxPrice()) > 0) {
            throw ApiException.badRequest("Le prix minimum doit etre inferieur ou egal au prix maximum");
        }

        List<Criteria> filters = new ArrayList<>();
        filters.add(Criteria.where("status").is(ServiceStatus.ACTIVE));
        if (hasText(c.q())) {
            Pattern keyword = containsIgnoreCase(c.q());
            filters.add(new Criteria().orOperator(
                    Criteria.where("title").regex(keyword),
                    Criteria.where("description").regex(keyword)));
        }
        if (c.category() != null) {
            filters.add(Criteria.where("category").is(c.category()));
        }
        if (hasText(c.city())) {
            filters.add(Criteria.where("city").regex(containsIgnoreCase(c.city())));
        }
        if (c.minPrice() != null || c.maxPrice() != null) {
            Criteria price = Criteria.where("price");
            if (c.minPrice() != null) {
                price = price.gte(new Decimal128(c.minPrice()));
            }
            if (c.maxPrice() != null) {
                price = price.lte(new Decimal128(c.maxPrice()));
            }
            filters.add(price);
        }

        Query query = new Query(new Criteria().andOperator(filters));
        long total = mongoTemplate.count(query, ServiceOffering.class);
        PageRequest pageable = PageRequest.of(page, size, sort.sort);
        List<ServiceOffering> content = mongoTemplate.find(query.with(pageable), ServiceOffering.class);
        Page<ServiceOffering> result = new PageImpl<>(content, pageable, total);
        return PageResponse.of(toResponses(content), result);
    }

    // ---------------------------------------------------------------- utilitaires

    /** SEC-02 : 404 si le service n'existe pas, 403 s'il appartient a un autre prestataire. */
    ServiceOffering loadOwned(String providerId, String serviceId) {
        ServiceOffering service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> ApiException.notFound("Service introuvable"));
        if (!service.getProviderId().equals(providerId)) {
            throw ApiException.forbidden("Ce service ne vous appartient pas");
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
                .map(ProviderProfileService::toSummary).orElse(null);
        return ServiceResponse.from(s, provider);
    }

    /** Charge les profils prestataires en une seule requete (evite N+1). */
    private List<ServiceResponse> toResponses(List<ServiceOffering> services) {
        Set<String> providerIds = services.stream().map(ServiceOffering::getProviderId).collect(Collectors.toSet());
        Map<String, ProviderSummary> summaries = profileRepository.findByUserIdIn(providerIds).stream()
                .map(ProviderProfileService::toSummary)
                .collect(Collectors.toMap(ProviderSummary::providerId, Function.identity(), (a, b) -> a));
        return services.stream().map(s -> ServiceResponse.from(s, summaries.get(s.getProviderId()))).toList();
    }

    private static Pattern containsIgnoreCase(String text) {
        return Pattern.compile(Pattern.quote(text.trim()), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
