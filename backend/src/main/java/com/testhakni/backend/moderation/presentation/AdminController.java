package com.testhakni.backend.moderation.presentation;

import java.util.List;
import java.util.Map;

import com.testhakni.backend.catalog.application.ServiceOfferingService;
import com.testhakni.backend.catalog.application.dto.ServiceResponse;
import com.testhakni.backend.identity.application.dto.UserResponse;
import com.testhakni.backend.identity.domain.model.AccountStatus;
import com.testhakni.backend.identity.domain.model.AuthenticatedUser;
import com.testhakni.backend.identity.domain.model.Role;
import com.testhakni.backend.moderation.application.AdminService;
import com.testhakni.backend.moderation.application.dto.ModerationPayloads.Decision;
import com.testhakni.backend.moderation.application.dto.ModerationPayloads.Resolution;
import com.testhakni.backend.moderation.application.dto.ModerationPayloads.Signalement;
import com.testhakni.backend.moderation.domain.model.ContentType;
import com.testhakni.backend.moderation.domain.model.Report;
import com.testhakni.backend.review.domain.model.Review;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Administration et signalements")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService admin;
    private final ServiceOfferingService catalogue;

    @Operation(summary = "Admin · statistiques de la plateforme")
    @GetMapping("/admin/dashboard")
    public Map<String, Long> dashboard() { return admin.dashboard(); }

    @Operation(summary = "Admin · rechercher les utilisateurs")
    @GetMapping("/admin/users")
    public List<UserResponse> users(@RequestParam(required = false) Role role, @RequestParam(required = false) AccountStatus status) {
        return admin.users(role, status);
    }

    @Operation(summary = "Admin · consulter un utilisateur")
    @GetMapping("/admin/users/{id}")
    public UserResponse user(@PathVariable String id) { return admin.user(id); }

    @Operation(summary = "Admin · activer ou suspendre un compte")
    @PatchMapping("/admin/users/{id}")
    public void account(@AuthenticationPrincipal AuthenticatedUser me, @PathVariable String id, @Valid @RequestBody Decision body) {
        admin.account(me.id(), id, body);
    }

    @Operation(summary = "Admin · consulter les services")
    @GetMapping("/admin/services")
    public List<ServiceResponse> services() { return catalogue.listAll(); }

    @Operation(summary = "Admin · consulter les avis")
    @GetMapping("/admin/reviews")
    public List<Review> reviews() { return admin.reviews(); }

    @Operation(summary = "Admin · modérer un service")
    @PatchMapping("/admin/services/{id}")
    public void service(@AuthenticationPrincipal AuthenticatedUser me, @PathVariable String id, @Valid @RequestBody Decision body) {
        admin.moderate(me.id(), ContentType.SERVICE, id, body);
    }

    @Operation(summary = "Admin · modérer un avis")
    @PatchMapping("/admin/reviews/{id}")
    public void review(@AuthenticationPrincipal AuthenticatedUser me, @PathVariable String id, @Valid @RequestBody Decision body) {
        admin.moderate(me.id(), ContentType.REVIEW, id, body);
    }

    @Operation(summary = "Utilisateur connecté · signaler un contenu")
    @PostMapping("/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public Report report(@AuthenticationPrincipal AuthenticatedUser me, @Valid @RequestBody Signalement body) {
        return admin.report(me.id(), body);
    }

    @Operation(summary = "Admin · consulter les signalements")
    @GetMapping("/admin/reports")
    public List<Report> reports() { return admin.reports(); }

    @Operation(summary = "Admin · traiter un signalement")
    @PatchMapping("/admin/reports/{id}")
    public void resolve(@AuthenticationPrincipal AuthenticatedUser me, @PathVariable String id, @Valid @RequestBody Resolution body) {
        admin.resolve(me.id(), id, body);
    }
}
