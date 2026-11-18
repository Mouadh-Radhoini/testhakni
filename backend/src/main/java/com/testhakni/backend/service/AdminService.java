package com.testhakni.backend.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import com.testhakni.backend.dto.*;
import com.testhakni.backend.dto.MarketplacePayloads.*;
import com.testhakni.backend.model.*;
import com.testhakni.backend.repository.*;
import com.testhakni.backend.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.*;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {
    private final MongoTemplate mongo;
    private final NotificationService notifications;
    private final BookingRepository bookings;
    private final ReportRepository reports;

    public Map<String,Long> dashboard() {
        return Map.of("users", count(User.class), "services", count(ServiceOffering.class),
                "requests", count(ServiceRequest.class), "offers", count(Offer.class), "bookings", count(Booking.class),
                "reviews", count(Review.class), "openReports", mongo.count(Query.query(Criteria.where("status").is(ReportStatus.OPEN)), Report.class));
    }

    private long count(Class<?> type) {
        return mongo.count(new Query(), type);
    }

    public List<UserResponse> users(Role role, AccountStatus status) {
        var query = new Query();
        if (role != null) query.addCriteria(Criteria.where("role").is(role));
        if (status != null) query.addCriteria(Criteria.where("status").is(status));
        return mongo.find(query, User.class).stream().map(UserResponse::from).toList();
    }

    public <T> T find(String id, Class<T> type) {
        T item = mongo.findById(id, type);
        if (item == null) throw ApiException.notFound("Ressource introuvable");
        return item;
    }

    private void audit(String actor, String type, String id, String decision, String reason) {
        mongo.insert(new ModerationAction(null, actor, type, id, decision, reason.trim(), Instant.now()));
        log.info("moderation actor={} type={} target={} decision={}", actor, type, id, decision);
    }

    @Transactional
    public void account(String actor, String id, Decision body) {
        var user = find(id, User.class);
        if (user.getRole() == Role.ADMIN && !body.enabled()) throw ApiException.conflict("Compte administrateur protégé");
        var status = body.enabled() ? AccountStatus.ACTIVE : AccountStatus.SUSPENDED;
        mongo.updateFirst(byId(id), Update.update("status", status).currentDate("updatedAt"), User.class);
        audit(actor, "USER", id, status.name(), body.reason());
    }

    private Query byId(String id) {
        return Query.query(Criteria.where("_id").is(id));
    }

    @Transactional
    public void moderate(String actor, ContentType type, String id, Decision body) {
        String recipient;
        if (type == ContentType.SERVICE) {
            var service = find(id, ServiceOffering.class);
            recipient = service.getProviderId();
            mongo.updateFirst(byId(id), Update.update("status", body.enabled() ? ServiceStatus.ACTIVE : ServiceStatus.SUSPENDED)
                    .currentDate("updatedAt"), ServiceOffering.class);
        } else {
            var review = find(id, Review.class);
            recipient = bookings.findById(review.bookingId()).orElseThrow().customerId();
            mongo.updateFirst(byId(id), Update.update("hidden", !body.enabled()), Review.class);
        }
        audit(actor, type.name(), id, body.enabled() ? "VISIBLE" : "HIDDEN", body.reason());
        notifications.send(recipient, "MODERATION", id, "Décision de modération : " + body.reason().trim());
    }

    public Report report(String user, Signalement body) {
        if (body.targetType() == ContentType.SERVICE) {
            if (find(body.targetId(), ServiceOffering.class).getStatus() != ServiceStatus.ACTIVE)
                throw ApiException.notFound("Service introuvable");
        } else if (find(body.targetId(), Review.class).hidden()) throw ApiException.notFound("Avis introuvable");
        return reports.insert(new Report(null, user, body.targetType(), body.targetId(), body.reason().trim(),
                ReportStatus.OPEN, Instant.now(), null, null));
    }

    @Transactional
    public void resolve(String actor, String id, Resolution body) {
        find(id, Report.class);
        if (body.status() == ReportStatus.OPEN) throw ApiException.badRequest("Choisissez une décision finale");
        var result = mongo.updateFirst(Query.query(Criteria.where("_id").is(id).and("status").is(ReportStatus.OPEN)),
                Update.update("status", body.status()).set("moderatorId", actor).set("decision", body.reason().trim()), Report.class);
        if (result.getModifiedCount() != 1) throw ApiException.conflict("Signalement déjà traité");
        audit(actor, "REPORT", id, body.status().name(), body.reason());
    }
}
