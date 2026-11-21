package com.testhakni.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import com.testhakni.backend.dto.*;
import com.testhakni.backend.dto.MarketplacePayloads.*;
import com.testhakni.backend.model.*;
import com.testhakni.backend.repository.*;
import com.testhakni.backend.service.AdminService;
import com.testhakni.backend.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Administration et signalements")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService admin;
    private final ServiceOfferingRepository services;
    private final ReviewRepository reviews;
    private final ReportRepository reports;

    @Operation(summary = "Admin · statistiques de la plateforme")
    @GetMapping("/admin/dashboard")
    public Map<String,Long> dashboard() { return admin.dashboard(); }

    @Operation(summary = "Admin · rechercher les utilisateurs")
    @GetMapping("/admin/users")
    public List<UserResponse> users(@RequestParam(required=false) Role role, @RequestParam(required=false) AccountStatus status) {
        return admin.users(role, status);
    }

    @Operation(summary = "Admin · consulter un utilisateur")
    @GetMapping("/admin/users/{id}")
    public UserResponse user(@PathVariable String id) { return UserResponse.from(admin.find(id, User.class)); }

    @Operation(summary = "Admin · activer ou suspendre un compte")
    @PatchMapping("/admin/users/{id}")
    public void account(@AuthenticationPrincipal AuthenticatedUser me, @PathVariable String id, @Valid @RequestBody Decision body) {
        admin.account(me.id(), id, body);
    }

    @Operation(summary = "Admin · consulter les services")
    @GetMapping("/admin/services")
    public List<ServiceResponse> services() {
        return services.findAll().stream().map(s -> ServiceResponse.from(s, null)).toList();
    }

    @Operation(summary = "Admin · consulter les avis")
    @GetMapping("/admin/reviews")
    public List<Review> reviews() { return reviews.findAll(); }

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
    public List<Report> reports() { return reports.findAll(); }

    @Operation(summary = "Admin · traiter un signalement")
    @PatchMapping("/admin/reports/{id}")
    public void resolve(@AuthenticationPrincipal AuthenticatedUser me, @PathVariable String id, @Valid @RequestBody Resolution body) {
        admin.resolve(me.id(), id, body);
    }
}
