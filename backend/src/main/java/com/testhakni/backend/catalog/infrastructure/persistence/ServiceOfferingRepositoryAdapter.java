package com.testhakni.backend.catalog.infrastructure.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.testhakni.backend.catalog.domain.model.ServiceOffering;
import com.testhakni.backend.catalog.domain.model.ServiceStatus;
import com.testhakni.backend.catalog.domain.repository.ServiceOfferingRepository;
import com.testhakni.backend.shared.application.PageResponse;
import com.testhakni.backend.shared.domain.Text;
import org.bson.types.Decimal128;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/** Adaptateur MongoDB du port {@link ServiceOfferingRepository}, dont la recherche multi-criteres. */
@Repository
class ServiceOfferingRepositoryAdapter implements ServiceOfferingRepository {

    private final ServiceOfferingMongoRepository repository;
    private final MongoTemplate mongo;

    ServiceOfferingRepositoryAdapter(ServiceOfferingMongoRepository repository, MongoTemplate mongo) {
        this.repository = repository;
        this.mongo = mongo;
    }

    @Override
    public Optional<ServiceOffering> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public ServiceOffering save(ServiceOffering service) {
        return repository.save(service);
    }

    @Override
    public List<ServiceOffering> saveAll(List<ServiceOffering> services) {
        return repository.saveAll(services);
    }

    @Override
    public void delete(ServiceOffering service) {
        repository.delete(service);
    }

    @Override
    public List<ServiceOffering> findByProviderId(String providerId) {
        return repository.findByProviderIdOrderByCreatedAtDesc(providerId);
    }

    @Override
    public List<ServiceOffering> findByProviderIdAndStatus(String providerId, ServiceStatus status) {
        return repository.findByProviderIdAndStatusOrderByCreatedAtDesc(providerId, status);
    }

    @Override
    public List<ServiceOffering> findAll() {
        return repository.findAll();
    }

    @Override
    public PageResponse<ServiceOffering> searchActive(SearchCriteria c, int page, int size, SortOption sort) {
        List<Criteria> filters = new ArrayList<>();
        filters.add(Criteria.where("status").is(ServiceStatus.ACTIVE));
        if (Text.hasText(c.q())) {
            Pattern keyword = containsIgnoreCase(c.q());
            filters.add(new Criteria().orOperator(
                    Criteria.where("title").regex(keyword),
                    Criteria.where("description").regex(keyword)));
        }
        if (c.category() != null) {
            filters.add(Criteria.where("category").is(c.category()));
        }
        if (Text.hasText(c.city())) {
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
        long total = mongo.count(query, ServiceOffering.class);
        List<ServiceOffering> content = mongo.find(query.with(PageRequest.of(page, size, toSort(sort))),
                ServiceOffering.class);
        return PageResponse.of(content, page, size, total);
    }

    @Override
    public void updateStatus(String id, ServiceStatus status) {
        mongo.updateFirst(Query.query(Criteria.where("_id").is(id)),
                Update.update("status", status).currentDate("updatedAt"), ServiceOffering.class);
    }

    @Override
    public long count() {
        return repository.count();
    }

    private static Sort toSort(SortOption option) {
        Sort newest = Sort.by(Sort.Direction.DESC, "createdAt");
        return switch (option) {
            case NEWEST -> newest;
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "price").and(newest);
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "price").and(newest);
        };
    }

    private static Pattern containsIgnoreCase(String text) {
        return Pattern.compile(Pattern.quote(text.trim()), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }
}
