package com.testhakni.backend.provider.presentation;

import com.testhakni.backend.provider.application.dto.ProviderProfileRequest;
import com.testhakni.backend.provider.application.dto.ProviderProfileResponse;
import com.testhakni.backend.provider.application.dto.ProviderPublicResponse;
import com.testhakni.backend.identity.domain.model.AuthenticatedUser;
import com.testhakni.backend.provider.application.ProviderProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/providers")
@Tag(name = "Prestataires", description = "Profil professionnel (PROV-01 a PROV-03, SEARCH-04)")
public class ProviderController {

    private final ProviderProfileService profileService;

    public ProviderController(ProviderProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me/profile")
    @Operation(summary = "Mon profil professionnel (PROVIDER)")
    public ProviderProfileResponse myProfile(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me) {
        return profileService.getMine(me.id());
    }

    @PutMapping("/me/profile")
    @Operation(summary = "Creer ou modifier mon profil professionnel (PROVIDER)")
    public ProviderProfileResponse upsertProfile(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me,
                                                 @Valid @RequestBody ProviderProfileRequest request) {
        return profileService.upsertMine(me.id(), request);
    }

    @GetMapping("/{providerId}")
    @SecurityRequirements
    @Operation(summary = "Profil public d'un prestataire et ses services actifs",
            description = "Sans email ni telephone. 404 si le prestataire n'existe pas.")
    public ProviderPublicResponse publicProfile(@PathVariable String providerId,
                                                @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser viewer) {
        return profileService.getPublic(providerId, viewer);
    }
}
