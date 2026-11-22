package com.testhakni.backend.shared.domain;

import java.util.Locale;

/** Petites regles de normalisation de texte partagees par les fonctionnalites. */
public final class Text {

    private Text() {
    }

    /** null si vide ou blanc, sinon la valeur sans espaces autour. */
    public static String trimToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    public static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    /** Les emails sont stockes et compares en minuscules. */
    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
