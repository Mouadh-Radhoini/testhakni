package com.testhakni.backend.catalog.domain.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.testhakni.backend.catalog.domain.model.ServiceCategory;
import com.testhakni.backend.catalog.domain.model.ServiceOffering;
import com.testhakni.backend.catalog.domain.model.ServiceStatus;
import com.testhakni.backend.shared.application.PageResponse;

/** Port du domaine : services publies par les prestataires. */
public interface ServiceOfferingRepository {

    /** Filtres de recherche publique (tous optionnels). */
    record SearchCriteria(String q, ServiceCategory category, String city,
                          BigDecimal minPrice, BigDecimal maxPrice) {
    }

    /** Tris acceptes par la recherche publique. */
    enum SortOption {
        NEWEST, PRICE_ASC, PRICE_DESC
    }

    Optional<ServiceOffering> findById(String id);

    ServiceOffering save(ServiceOffering service);

    List<ServiceOffering> saveAll(List<ServiceOffering> services);

    void delete(ServiceOffering service);

    /** Du plus recent au plus ancien. */
    List<ServiceOffering> findByProviderId(String providerId);

    /** Du plus recent au plus ancien. */
    List<ServiceOffering> findByProviderIdAndStatus(String providerId, ServiceStatus status);

    List<ServiceOffering> findAll();

    /** Recherche paginee parmi les services ACTIVE uniquement. */
    PageResponse<ServiceOffering> searchActive(SearchCriteria criteria, int page, int size, SortOption sort);

    void updateStatus(String id, ServiceStatus status);

    long count();
}
