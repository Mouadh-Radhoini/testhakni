package com.testhakni.backend.identity.application.port;

/** Port de sortie : hachage des mots de passe (implemente avec BCrypt). AUTH-07 */
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String hash);
}
