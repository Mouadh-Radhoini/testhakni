package com.testhakni.backend.identity.domain.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** Compte utilisateur (client, prestataire ou admin). AUTH-01 */
@Document("users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String id;

    /** Identifiant de connexion, toujours stocke en minuscules. */
    @Indexed(unique = true)
    private String email;

    /** Hash BCrypt, jamais renvoye par l'API. */
    private String passwordHash;

    private String firstName;
    private String lastName;
    private String phone;
    private String city;

    /** Photo de profil (id du fichier en GridFS), null si aucune. */
    private String avatarId;

    @Indexed
    private Role role;

    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    /** Nom public "Prenom N." : on n'expose pas le nom complet. */
    public String displayName() {
        String initial = lastName == null || lastName.isBlank() ? "" : " " + lastName.charAt(0) + ".";
        return firstName + initial;
    }
}
