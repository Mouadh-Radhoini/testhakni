package com.testhakni.backend.provider.application;

import java.util.ArrayList;
import java.util.List;

import com.testhakni.backend.catalog.application.dto.ServiceResponse;
import com.testhakni.backend.catalog.domain.model.ServiceStatus;
import com.testhakni.backend.catalog.domain.repository.ServiceOfferingRepository;
import com.testhakni.backend.identity.domain.model.Role;
import com.testhakni.backend.identity.domain.model.User;
import com.testhakni.backend.identity.domain.repository.UserRepository;
import com.testhakni.backend.provider.application.dto.ProviderProfileRequest;
import com.testhakni.backend.provider.application.dto.ProviderProfileResponse;
import com.testhakni.backend.provider.application.dto.ProviderPublicResponse;
import com.testhakni.backend.provider.application.dto.ProviderSummary;
import com.testhakni.backend.provider.domain.model.ProviderProfile;
import com.testhakni.backend.provider.domain.repository.ProviderProfileRepository;
import com.testhakni.backend.review.domain.model.RatingSummary;
import com.testhakni.backend.review.domain.repository.ReviewRepository;
import com.testhakni.backend.identity.domain.model.AuthenticatedUser;
import com.testhakni.backend.shared.application.MediaUrls;
import com.testhakni.backend.shared.domain.Text;
import com.testhakni.backend.shared.domain.exception.DomainException;
import org.springframework.stereotype.Service;

/** Cas d'usage : profil professionnel du prestataire. PROV-01 a PROV-03, SEARCH-04 */
@Service
public class ProviderProfileService {

    private final ProviderProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ServiceOfferingRepository serviceRepository;
    private final ReviewRepository reviews;

    public ProviderProfileService(ProviderProfileRepository profileRepository, UserRepository userRepository,
                                  ServiceOfferingRepository serviceRepository, ReviewRepository reviews) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.reviews = reviews;
    }

    public ProviderProfileResponse getMine(String providerId) {
        return profileRepository.findByUserId(providerId)
                .map(ProviderProfileResponse::from)
                .orElseThrow(() -> DomainException.notFound("Profil prestataire pas encore cree"));
    }

    /**
     * Cree le profil s'il n'existe pas, sinon le met a jour.
     * Le profil est toujours celui du prestataire connecte : impossible de modifier celui d'un autre (SEC-02).
     */
    public ProviderProfileResponse upsertMine(String providerId, ProviderProfileRequest req) {
        ProviderProfile profile = profileRepository.findByUserId(providerId)
                .orElseGet(() -> ProviderProfile.builder().userId(providerId).build());
        profile.setBusinessName(req.businessName().trim());
        profile.setBio(Text.trimToNull(req.bio()));
        profile.setCategories(new ArrayList<>(req.categories().stream().distinct().toList()));
        profile.setSkills(req.skills() == null ? new ArrayList<>()
                : new ArrayList<>(req.skills().stream().map(String::trim).distinct().toList()));
        profile.setCity(req.city().trim());
        profile.setServiceArea(Text.trimToNull(req.serviceArea()));
        profile.setYearsOfExperience(req.yearsOfExperience());
        profile.setAvailable(req.available() == null || req.available());
        return ProviderProfileResponse.from(profileRepository.save(profile));
    }

    /** Profil public : sans email ni telephone. 404 si le prestataire n'existe pas ou n'est pas actif. */
    public ProviderPublicResponse getPublic(String providerId) {
        return getPublic(providerId, null);
    }

    /** Variante sensible au visiteur : sans compte, les services sont montres en apercu sans prix. */
    public ProviderPublicResponse getPublic(String providerId, AuthenticatedUser viewer) {
        User user = userRepository.findById(providerId)
                .filter(u -> u.getRole() == Role.PROVIDER && u.isActive())
                .orElseThrow(() -> DomainException.notFound("Prestataire introuvable"));
        ProviderProfile profile = profileRepository.findByUserId(providerId)
                .orElseThrow(() -> DomainException.notFound("Prestataire introuvable"));

        ProviderSummary summary = ProviderSummary.from(profile);
        List<ServiceResponse> services = serviceRepository
                .findByProviderIdAndStatus(providerId, ServiceStatus.ACTIVE)
                .stream().map(s -> viewer == null ? ServiceResponse.teaser(s) : ServiceResponse.from(s, summary)).toList();

        var rating = reviews.rating(providerId);
        return new ProviderPublicResponse(providerId, user.displayName(), profile.getBusinessName(),
                profile.getBio(), profile.getCategories(), profile.getSkills(), profile.getCity(),
                profile.getServiceArea(), profile.getYearsOfExperience(), profile.isAvailable(),
                user.getCreatedAt(), services, rating.map(RatingSummary::average).orElse(null),
                rating.map(RatingSummary::count).orElse(0L),
                MediaUrls.of(user.getAvatarId()), MediaUrls.of(profile.getPhotoIds()));
    }
}
