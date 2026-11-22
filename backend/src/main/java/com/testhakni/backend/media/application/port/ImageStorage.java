package com.testhakni.backend.media.application.port;

import java.util.Optional;

import com.testhakni.backend.media.domain.ImageFormat;

/** Port de sortie : stockage binaire des images (implemente avec MongoDB GridFS). */
public interface ImageStorage {

    /** Image relue : contenu et type MIME a renvoyer. */
    record StoredImage(byte[] content, String mediaType) {
    }

    /** Enregistre l'image et renvoie son identifiant. */
    String store(byte[] content, ImageFormat format, String ownerId);

    Optional<StoredImage> load(String id);

    void delete(String id);
}
