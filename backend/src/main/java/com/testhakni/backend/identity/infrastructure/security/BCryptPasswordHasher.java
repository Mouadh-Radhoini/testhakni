package com.testhakni.backend.identity.infrastructure.security;

import com.testhakni.backend.identity.application.port.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Adaptateur du port {@link PasswordHasher} base sur le PasswordEncoder Spring (BCrypt). */
@Component
public class BCryptPasswordHasher implements PasswordHasher {

    private final PasswordEncoder encoder;

    public BCryptPasswordHasher(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hash) {
        return encoder.matches(rawPassword, hash);
    }
}
