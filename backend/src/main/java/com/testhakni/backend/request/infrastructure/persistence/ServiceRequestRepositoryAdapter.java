package com.testhakni.backend.request.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.testhakni.backend.catalog.domain.model.ServiceCategory;
import com.testhakni.backend.request.domain.model.RequestStatus;
import com.testhakni.backend.request.domain.model.ServiceRequest;
import com.testhakni.backend.request.domain.repository.ServiceRequestRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/** Adaptateur MongoDB du port {@link ServiceRequestRepository}. */
@Repository
class ServiceRequestRepositoryAdapter implements ServiceRequestRepository {

    private final ServiceRequestMongoRepository repository;
    private final MongoTemplate mongo;

    ServiceRequestRepositoryAdapter(ServiceRequestMongoRepository repository, MongoTemplate mongo) {
        this.repository = repository;
        this.mongo = mongo;
    }

    @Override
    public Optional<ServiceRequest> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public boolean existsById(String id) {
        return repository.existsById(id);
    }

    @Override
    public ServiceRequest save(ServiceRequest request) {
        return repository.save(request);
    }

    @Override
    public List<ServiceRequest> findByCustomerId(String customerId) {
        return repository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Override
    public List<ServiceRequest> findByCustomerIdAndStatus(String customerId, RequestStatus status) {
        return repository.findByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, status);
    }

    @Override
    public List<ServiceRequest> findOpenMatching(Collection<ServiceCategory> categories, String city) {
        return mongo.find(Query.query(openMatching(categories, city)).with(Sort.by("createdAt").descending()),
                ServiceRequest.class);
    }

    @Override
    public Optional<ServiceRequest> touchIfOpenMatching(String id, Collection<ServiceCategory> categories, String city) {
        return Optional.ofNullable(mongo.findAndModify(Query.query(openMatching(categories, city).and("_id").is(id)),
                new Update().inc("offerRevision", 1), ServiceRequest.class));
    }

    @Override
    public boolean changeStatus(String id, RequestStatus expected, RequestStatus target) {
        var result = mongo.updateFirst(Query.query(Criteria.where("_id").is(id).and("status").is(expected)),
                Update.update("status", target).currentDate("updatedAt"), ServiceRequest.class);
        return result.getModifiedCount() == 1;
    }

    @Override
    public long count() {
        return repository.count();
    }

    private static Criteria openMatching(Collection<ServiceCategory> categories, String city) {
        return Criteria.where("status").is(RequestStatus.OPEN).and("category").in(categories)
                .and("city").regex(Pattern.compile("^" + Pattern.quote(city) + "$",
                        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
    }
}
