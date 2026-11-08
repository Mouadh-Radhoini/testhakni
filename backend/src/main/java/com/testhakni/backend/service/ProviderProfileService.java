package com.testhakni.backend.service;

import java.util.ArrayList;
import java.util.List;

import com.testhakni.backend.dto.ProviderProfileRequest;
import com.testhakni.backend.dto.ProviderProfileResponse;
import com.testhakni.backend.dto.ProviderPublicResponse;
import com.testhakni.backend.dto.ProviderSummary;
import com.testhakni.backend.dto.ServiceResponse;
import com.testhakni.backend.exception.ApiException;
import com.testhakni.backend.model.ProviderProfile;
import com.testhakni.backend.model.Role;
import com.testhakni.backend.model.ServiceStatus;
import com.testhakni.backend.model.User;
import com.testhakni.backend.repository.ProviderProfileRepository;
import com.testhakni.backend.repository.ServiceOfferingRepository;
import com.testhakni.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

/** Profil professionnel du prestataire. PROV-01 a PROV-03, SEARCH-04 */
@Service
public class ProviderProfileService {

    private final ProviderProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ServiceOfferingRepository serviceRepository;

    public ProviderProfileService(ProviderProfileRepository profileRepository, UserRepository userRepository,
                                  ServiceOfferingRepository serviceRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
    }

    public ProviderProfileResponse getMine(String providerId) {
        return profileRepository.findByUserId(providerId)
                .map(ProviderProfileResponse::from)
                .orElseThrow(() -> ApiException.notFound("Profil prestataire pas encore cree"));
    }

    /**
     * Cree le profil s'il n'existe pas, sinon le met a jour.
     * Le profil est toujours celui du prestataire connecte : impossible de modifier celui d'un autre (SEC-02).
     */
    public ProviderProfileResponse upsertMine(String providerId, ProviderProfileRequest req) {
        ProviderProfile profile = profileRepository.findByUserId(providerId)
                .orElseGet(() -> ProviderProfile.builder().userId(providerId).build());
        profile.setBusinessName(req.businessName().trim());
        profile.setBio(AuthService.trimToNull(req.bio()));
        profile.setCategories(new ArrayList<>(req.categories().stream().distinct().toList()));
        profile.setSkills(req.skills() == null ? new ArrayList<>()
                : new ArrayList<>(req.skills().stream().map(String::trim).distinct().toList()));
        profile.setCity(req.city().trim());
        profile.setServiceArea(AuthService.trimToNull(req.serviceArea()));
        profile.setYearsOfExperience(req.yearsOfExperience());
        profile.setAvailable(req.available() == null || req.available());
        return ProviderProfileResponse.from(profileRepository.save(profile));
    }

    /** Profil public : sans email ni telephone. 404 si le prestataire n'existe pas ou n'est pas actif. */
    public ProviderPublicResponse getPublic(String providerId) {
        User user = userRepository.findById(providerId)
                .filter(u -> u.getRole() == Role.PROVIDER && u.isActive())
                .orElseThrow(() -> ApiException.notFound("Prestataire introuvable"));
        ProviderProfile profile = profileRepository.findByUserId(providerId)
                .orElseThrow(() -> ApiException.notFound("Prestataire introuvable"));

        ProviderSummary summary = toSummary(profile);
        List<ServiceResponse> services = serviceRepository
                .findByProviderIdAndStatusOrderByCreatedAtDesc(providerId, ServiceStatus.ACTIVE)
                .stream().map(s -> ServiceResponse.from(s, summary)).toList();

        return new ProviderPublicResponse(providerId, displayName(user), profile.getBusinessName(),
                profile.getBio(), profile.getCategories(), profile.getSkills(), profile.getCity(),
                profile.getServiceArea(), profile.getYearsOfExperience(), profile.isAvailable(),
                user.getCreatedAt(), services);
    }

    static ProviderSummary toSummary(ProviderProfile p) {
        return new ProviderSummary(p.getUserId(), p.getBusinessName(), p.getCity(),
                p.getYearsOfExperience(), p.isAvailable());
    }

    /** "Prenom N." : on n'expose pas le nom complet. */
    static String displayName(User u) {
        String initial = u.getLastName() == null || u.getLastName().isBlank() ? "" : " " + u.getLastName().charAt(0) + ".";
        return u.getFirstName() + initial;
    }
}
