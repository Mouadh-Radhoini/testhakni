package com.testhakni.backend.notification.presentation;

import com.testhakni.backend.identity.domain.model.AuthenticatedUser;
import com.testhakni.backend.notification.application.NotificationService;
import com.testhakni.backend.notification.domain.model.Notification;
import com.testhakni.backend.shared.application.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notifications")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notifications;

    @Operation(summary = "Mes notifications paginées")
    @GetMapping
    public PageResponse<Notification> notifications(@AuthenticationPrincipal AuthenticatedUser user,
                                                    @RequestParam(defaultValue = "0") @Min(0) int page) {
        return notifications.list(user.id(), page);
    }

    @Operation(summary = "Nombre de notifications non lues")
    @GetMapping("/unread")
    public long unread(@AuthenticationPrincipal AuthenticatedUser user) {
        return notifications.unread(user.id());
    }

    @Operation(summary = "Marquer ma notification comme lue")
    @PatchMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void read(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String id) {
        notifications.read(user.id(), id);
    }
}
