package com.testhakni.backend.moderation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.testhakni.backend.booking.domain.repository.BookingRepository;
import com.testhakni.backend.catalog.domain.model.ServiceOffering;
import com.testhakni.backend.catalog.domain.model.ServiceStatus;
import com.testhakni.backend.catalog.domain.repository.ServiceOfferingRepository;
import com.testhakni.backend.identity.domain.model.Role;
import com.testhakni.backend.identity.domain.model.User;
import com.testhakni.backend.identity.domain.repository.RefreshTokenRepository;
import com.testhakni.backend.identity.domain.repository.UserRepository;
import com.testhakni.backend.moderation.application.AdminService;
import com.testhakni.backend.moderation.application.dto.ModerationPayloads.Signalement;
import com.testhakni.backend.moderation.domain.model.ContentType;
import com.testhakni.backend.moderation.domain.model.Report;
import com.testhakni.backend.moderation.domain.model.ReportStatus;
import com.testhakni.backend.moderation.domain.repository.ModerationActionRepository;
import com.testhakni.backend.moderation.domain.repository.ReportRepository;
import com.testhakni.backend.notification.application.NotificationService;
import com.testhakni.backend.offer.domain.repository.OfferRepository;
import com.testhakni.backend.request.domain.repository.ServiceRequestRepository;
import com.testhakni.backend.review.domain.repository.ReviewRepository;
import org.junit.jupiter.api.Test;

/** Au 3e signalement ouvert sur un meme contenu, les admins sont notifies ; avant, rien ne se passe. */
class ReportAlertTest {
    private final UserRepository users = mock(UserRepository.class);
    private final ServiceOfferingRepository services = mock(ServiceOfferingRepository.class);
    private final ReportRepository reports = mock(ReportRepository.class);
    private final NotificationService notifications = mock(NotificationService.class);
    private final AdminService admin = new AdminService(users, services, mock(ServiceRequestRepository.class),
            mock(OfferRepository.class), mock(BookingRepository.class), mock(ReviewRepository.class), reports,
            mock(ModerationActionRepository.class), notifications, mock(RefreshTokenRepository.class));

    private void arrange(long openReports) {
        ServiceOffering service = mock(ServiceOffering.class);
        when(service.getStatus()).thenReturn(ServiceStatus.ACTIVE);
        when(services.findById("svc-1")).thenReturn(Optional.of(service));
        when(reports.insert(any(Report.class))).thenAnswer(call -> call.getArgument(0));
        when(reports.countByTarget(ContentType.SERVICE, "svc-1", ReportStatus.OPEN)).thenReturn(openReports);
        User boss = mock(User.class);
        when(boss.getId()).thenReturn("admin-1");
        when(users.search(Role.ADMIN, null)).thenReturn(List.of(boss));
    }

    @Test
    void notifiesAdminsOnTheThirdOpenReport() {
        arrange(3);
        admin.report("customer-1", new Signalement(ContentType.SERVICE, "svc-1", "Contenu douteux"));
        verify(notifications).send(eq("admin-1"), eq("REPORT_ALERT"), eq("svc-1"), any());
    }

    @Test
    void staysSilentBeforeTheThirdReport() {
        arrange(2);
        admin.report("customer-1", new Signalement(ContentType.SERVICE, "svc-1", "Contenu douteux"));
        verify(notifications, never()).send(any(), any(), any(), any());
    }
}
