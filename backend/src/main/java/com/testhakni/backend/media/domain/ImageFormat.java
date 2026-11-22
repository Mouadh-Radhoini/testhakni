package com.testhakni.backend.media.domain;

import java.util.Optional;

/**
 * Formats d'image acceptes, reconnus a leur signature binaire ("magic bytes") et non a
 * l'extension ou au Content-Type annonces par le client, qui peuvent mentir. SEC-08
 */
public enum ImageFormat {
    JPEG("image/jpeg"),
    PNG("image/png"),
    WEBP("image/webp");

    /** Taille maximale d'une image envoyee. */
    public static final int MAX_BYTES = 2 * 1024 * 1024;

    private final String mediaType;

    ImageFormat(String mediaType) {
        this.mediaType = mediaType;
    }

    public String mediaType() {
        return mediaType;
    }

    /** Detecte le format reel du contenu ; vide si ce n'est pas une image acceptee. */
    public static Optional<ImageFormat> detect(byte[] b) {
        if (b == null || b.length < 12) return Optional.empty();
        if ((b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) return Optional.of(JPEG);
        if ((b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) return Optional.of(PNG);
        if (b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return Optional.of(WEBP);
        return Optional.empty();
    }
}
