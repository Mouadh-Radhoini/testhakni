package com.testhakni.backend.media.presentation;

import java.io.IOException;
import java.time.Duration;

import com.testhakni.backend.identity.application.dto.UserResponse;
import com.testhakni.backend.identity.domain.model.AuthenticatedUser;
import com.testhakni.backend.media.application.ImageService;
import com.testhakni.backend.media.application.port.ImageStorage.StoredImage;
import com.testhakni.backend.provider.application.dto.ProviderProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@Tag(name = "Images", description = "Photo de profil et photos de realisations (JPEG, PNG ou WebP, 2 Mo max)")
public class MediaController {

    private final ImageService images;

    public MediaController(ImageService images) {
        this.images = images;
    }

    @GetMapping("/media/{id}")
    @SecurityRequirements
    @Operation(summary = "Lire une image (public)")
    public ResponseEntity<byte[]> image(@PathVariable String id) {
        StoredImage image = images.load(id);
        // Contenu immuable (un nouvel envoi cree un nouvel id) : cache long cote navigateur
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.mediaType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic().immutable())
                .header("Content-Disposition", "inline")
                .body(image.content());
    }

    @PutMapping(value = "/users/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Changer ma photo de profil")
    public UserResponse setAvatar(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me,
                                  @RequestPart("file") MultipartFile file) throws IOException {
        return images.setAvatar(me.id(), file.getBytes());
    }

    @DeleteMapping("/users/me/avatar")
    @Operation(summary = "Supprimer ma photo de profil")
    public UserResponse removeAvatar(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me) {
        return images.removeAvatar(me.id());
    }

    @PostMapping(value = "/providers/me/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Ajouter une photo de realisation (PROVIDER, 6 max)")
    public ProviderProfileResponse addPhoto(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me,
                                            @RequestPart("file") MultipartFile file) throws IOException {
        return images.addPortfolioPhoto(me.id(), file.getBytes());
    }

    @DeleteMapping("/providers/me/photos/{photoId}")
    @Operation(summary = "Supprimer une photo de realisation (PROVIDER)")
    public ProviderProfileResponse removePhoto(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me,
                                               @PathVariable String photoId) {
        return images.removePortfolioPhoto(me.id(), photoId);
    }
}
