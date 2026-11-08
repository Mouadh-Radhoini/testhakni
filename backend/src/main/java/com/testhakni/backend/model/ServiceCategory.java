package com.testhakni.backend.model;

/** Taxonomie des services de la marketplace. SERV-05 */
public enum ServiceCategory {
    PLOMBERIE("Plomberie"),
    ELECTRICITE("Électricité"),
    MENAGE("Ménage"),
    JARDINAGE("Jardinage"),
    PEINTURE("Peinture"),
    BRICOLAGE("Bricolage"),
    DEMENAGEMENT("Déménagement"),
    INFORMATIQUE("Informatique"),
    COURS_PARTICULIERS("Cours particuliers"),
    AUTRE("Autre");

    private final String label;

    ServiceCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
