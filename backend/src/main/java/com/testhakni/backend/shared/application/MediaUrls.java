package com.testhakni.backend.shared.application;

import java.util.List;

/** Construit l'URL publique d'une image stockee (servie par GET /api/media/{id}). */
public final class MediaUrls {

    public static final String PREFIX = "/api/media/";

    private MediaUrls() {
    }

    public static String of(String mediaId) {
        return mediaId == null ? null : PREFIX + mediaId;
    }

    public static List<String> of(List<String> mediaIds) {
        return mediaIds == null ? List.of() : mediaIds.stream().map(MediaUrls::of).toList();
    }
}
