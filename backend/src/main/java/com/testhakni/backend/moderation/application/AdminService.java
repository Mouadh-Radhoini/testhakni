package com.testhakni.backend.moderation.application;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.testhakni.backend.booking.domain.repository.BookingRepository;
import com.testhakni.backend.catalog.domain.model.ServiceOffering;
import com.testhakni.backend.catalog.domain.model.ServiceStatus;
import com.testhakni.backend.catalog.domain.repository.ServiceOfferingRepository;
import com.testhakni.backend.identity.application.dto.UserResponse;
import com.testhakni.backend.identity.domain.model.AccountStatus;
import com.testhakni.backend.identity.domain.model.Role;
import com.testhakni.backend.identity.domain.model.User;
import com.testhakni.backend.identity.domain.repository.RefreshTokenRepository;
import com.testhakni.backend.identity.domain.repository.UserRepository;
import com.testhakni.backend.moderation.application.dto.ModerationPayloads.Decision;
import com.testhakni.backend.moderation.application.dto.ModerationPayloads.Resolution;
import com.testhakni.backend.moderation.application.dto.ModerationPayloads.Signalement;
import com.testhakni.backend.moderation.domain.model.ContentType;
import com.testhakni.backend.moderation.domain.model.ModerationAction;
import com.testhakni.backend.moderation.domain.model.Report;
import com.testhakni.backend.moderation.domain.model.ReportStatus;
import com.testhakni.backend.moderation.domain.repository.ModerationActionRepository;
import com.testhakni.backend.moderation.domain.repository.ReportRepository;
import com.testhakni.backend.notification.application.NotificationService;
import com.testhakni.backend.offer.domain.repository.OfferRepository;
import com.testhakni.backend.request.domain.repository.ServiceRequestRepository;
import com.testhakni.backend.review.domain.model.Review;
import com.testhakni.backend.review.domain.repository.ReviewRepository;
import com.testhakni.backend.shared.domain.exception.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cas d'usage : administration de la plateforme, moderation et signalements. */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {
    private static final String NOT_FOUND = "Ressource introuvable";

    private final UserRepository users;
    private final ServiceOfferingRepository services;
    private final ServiceRequestRepository requests;
    private final OfferRepository offers;
    private final BookingRepository bookings;
    private final ReviewRepository reviews;
    private final ReportRepository reports;
    private final ModerationActionRepository auditLog;
    private final NotificationService notifications;
    private final RefreshTokenRepository refreshTokens;

    public Map<String, Long> dashboard() {
        return Map.of("users", users.count(), "services", services.count(),
                "requests", requests.count(), "offers", offers.count(), "bookings", bookings.count(),
                "reviews", reviews.count(), "openReports", reports.countByStatus(ReportStatus.OPEN));
    }

    public List<UserResponse> users(Role role, AccountStatus status) {
        return users.search(role, status).stream().map(UserResponse::from).toList();
    }

    public UserResponse user(String id) {
        return UserResponse.from(findUser(id));
    }

    public List<Review> reviews() {
        return reviews.findAll();
    }

    public List<Report> reports() {
        return reports.findAll();
    }

    private void audit(String actor, String type, String id, String decision, String reason) {
        auditLog.insert(new ModerationAction(null, actor, type, id, decision, reason.trim(), Instant.now()));
        log.info("moderation actor={} type={} target={} decision={}", actor, type, id, decision);
    }

    @Transactional
    public void account(String actor, String id, Decision body) {
        var user = findUser(id);
        if (user.getRole() == Role.ADMIN && !body.enabled()) throw DomainException.conflict("Compte administrateur protégé");
        var status = body.enabled() ? AccountStatus.ACTIVE : AccountStatus.SUSPENDED;
        users.updateStatus(id, status);
        if (status == AccountStatus.SUSPENDED) refreshTokens.revokeAllForUser(id, Instant.now());
        audit(actor, "USER", id, status.name(), body.reason());
    }

    @Transactional
    public void moderate(String actor, ContentType type, String id, Decision body) {
        String recipient;
        if (type == ContentType.SERVICE) {
            var service = findService(id);
            recipient = service.getProviderId();
            services.updateStatus(id, body.enabled() ? ServiceStatus.ACTIVE : ServiceStatus.SUSPENDED);
        } else {
            var review = findReview(id);
            recipient = bookings.findById(review.bookingId()).orElseThrow().customerId();
            reviews.setHidden(id, !body.enabled());
        }
        audit(actor, type.name(), id, body.enabled() ? "VISIBLE" : "HIDDEN", body.reason());
        notifications.send(recipient, "MODERATION", id, "Décision de modération : " + body.reason().trim());
    }

    public Report report(String user, Signalement body) {
        if (body.targetType() == ContentType.SERVICE) {
            if (findService(body.targetId()).getStatus() != ServiceStatus.ACTIVE)
                throw DomainException.notFound("Service introuvable");
        } else if (findReview(body.targetId()).hidden()) throw DomainException.notFound("Avis introuvable");
        Report saved = reports.insert(new Report(null, user, body.targetType(), body.targetId(), body.reason().trim(),
                ReportStatus.OPEN, Instant.now(), null, null));
        alertAdminsIfRepeated(saved);
        return saved;
    }

    /** Au 3e signalement ouvert sur un meme contenu, les admins sont prevenus (aucune suspension automatique). */
    private static final long ALERT_THRESHOLD = 3;

    private void alertAdminsIfRepeated(Report report) {
        long open = reports.countByTarget(report.targetType(), report.targetId(), ReportStatus.OPEN);
        if (open != ALERT_THRESHOLD) return;
        String what = report.targetType() == ContentType.SERVICE ? "Un service" : "Un avis";
        String message = what + " a reçu " + ALERT_THRESHOLD + " signalements ouverts : à examiner.";
        for (User admin : users.search(Role.ADMIN, null)) {
            notifications.send(admin.getId(), "MODERATION", report.targetId(), message);
        }
    }

    @Transactional
    public void resolve(String actor, String id, Resolution body) {
        reports.findById(id).orElseThrow(() -> DomainException.notFound(NOT_FOUND));
        if (body.status() == ReportStatus.OPEN) throw DomainException.badRequest("Choisissez une décision finale");
        if (!reports.resolveIfOpen(id, body.status(), actor, body.reason().trim())) {
            throw DomainException.conflict("Signalement déjà traité");
        }
        audit(actor, "REPORT", id, body.status().name(), body.reason());
    }

    private User findUser(String id) {
        return users.findById(id).orElseThrow(() -> DomainException.notFound(NOT_FOUND));
    }

    private ServiceOffering findService(String id) {
        return services.findById(id).orElseThrow(() -> DomainException.notFound(NOT_FOUND));
    }

    private Review findReview(String id) {
        return reviews.findById(id).orElseThrow(() -> DomainException.notFound(NOT_FOUND));
    }
}
