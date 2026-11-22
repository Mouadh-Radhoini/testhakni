package com.testhakni.backend.media.application;

import com.testhakni.backend.identity.application.dto.UserResponse;
import com.testhakni.backend.identity.domain.model.User;
import com.testhakni.backend.identity.domain.repository.UserRepository;
import com.testhakni.backend.media.application.port.ImageStorage;
import com.testhakni.backend.media.application.port.ImageStorage.StoredImage;
import com.testhakni.backend.media.domain.ImageFormat;
import com.testhakni.backend.provider.application.dto.ProviderProfileResponse;
import com.testhakni.backend.provider.domain.model.ProviderProfile;
import com.testhakni.backend.provider.domain.repository.ProviderProfileRepository;
import com.testhakni.backend.shared.domain.exception.DomainException;
import org.springframework.stereotype.Service;

/**
 * Cas d'usage : photo de profil des comptes et photos de realisations des prestataires.
 * Le contenu est verifie (format reel, taille) avant tout stockage. SEC-08
 */
@Service
public class ImageService {

    /** Nombre maximal de photos de realisations par prestataire. */
    public static final int MAX_PORTFOLIO_PHOTOS = 6;

    private final ImageStorage storage;
    private final UserRepository users;
    private final ProviderProfileRepository profiles;

    public ImageService(ImageStorage storage, UserRepository users, ProviderProfileRepository profiles) {
        this.storage = storage;
        this.users = users;
        this.profiles = profiles;
    }

    public StoredImage load(String id) {
        return storage.load(id).orElseThrow(() -> DomainException.notFound("Image introuvable"));
    }

    public UserResponse setAvatar(String userId, byte[] content) {
        User user = users.findById(userId).orElseThrow(() -> DomainException.notFound("Utilisateur introuvable"));
        String newId = storage.store(content, validate(content), userId);
        String oldId = user.getAvatarId();
        user.setAvatarId(newId);
        User saved = users.save(user);
        if (oldId != null) storage.delete(oldId);
        return UserResponse.from(saved);
    }

    public UserResponse removeAvatar(String userId) {
        User user = users.findById(userId).orElseThrow(() -> DomainException.notFound("Utilisateur introuvable"));
        String oldId = user.getAvatarId();
        user.setAvatarId(null);
        User saved = users.save(user);
        if (oldId != null) storage.delete(oldId);
        return UserResponse.from(saved);
    }

    public ProviderProfileResponse addPortfolioPhoto(String providerId, byte[] content) {
        ProviderProfile profile = profile(providerId);
        if (profile.getPhotoIds().size() >= MAX_PORTFOLIO_PHOTOS) {
            throw DomainException.conflict(MAX_PORTFOLIO_PHOTOS + " photos maximum : supprimez-en une d'abord");
        }
        String id = storage.store(content, validate(content), providerId);
        profile.getPhotoIds().add(id);
        return ProviderProfileResponse.from(profiles.save(profile));
    }

    public ProviderProfileResponse removePortfolioPhoto(String providerId, String photoId) {
        ProviderProfile profile = profile(providerId);
        // SEC-02 : seules les photos de son propre profil peuvent etre supprimees
        if (!profile.getPhotoIds().remove(photoId)) throw DomainException.notFound("Photo introuvable");
        ProviderProfileResponse response = ProviderProfileResponse.from(profiles.save(profile));
        storage.delete(photoId);
        return response;
    }

    private ProviderProfile profile(String providerId) {
        return profiles.findByUserId(providerId)
                .orElseThrow(() -> DomainException.notFound("Profil prestataire pas encore cree"));
    }

    static ImageFormat validate(byte[] content) {
        if (content == null || content.length == 0) throw DomainException.badRequest("Fichier vide");
        if (content.length > ImageFormat.MAX_BYTES) throw DomainException.badRequest("Image trop lourde (2 Mo maximum)");
        return ImageFormat.detect(content)
                .orElseThrow(() -> DomainException.badRequest("Format non accepte : JPEG, PNG ou WebP uniquement"));
    }
}
