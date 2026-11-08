package com.testhakni.backend.dto;

/** Regles de validation partagees. AUTH-07 */
public final class ValidationRules {

    /** 8 a 72 caracteres (limite BCrypt), au moins 1 minuscule, 1 majuscule, 1 chiffre et 1 caractere special. */
    public static final String PASSWORD_REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,72}$";

    public static final String PASSWORD_MESSAGE =
            "Le mot de passe doit contenir 8 a 72 caracteres, dont une majuscule, une minuscule, un chiffre et un caractere special";

    /** Telephone : chiffres, espaces, +, -, (), 8 a 20 caracteres. */
    public static final String PHONE_REGEX = "^[+0-9 ()-]{8,20}$";

    public static final String PHONE_MESSAGE = "Numero de telephone invalide";

    private ValidationRules() {
    }
}
