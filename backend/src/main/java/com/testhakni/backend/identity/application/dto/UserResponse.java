package com.testhakni.backend.identity.application.dto;

import java.time.Instant;

import com.testhakni.backend.identity.domain.model.AccountStatus;
import com.testhakni.backend.identity.domain.model.Role;
import com.testhakni.backend.identity.domain.model.User;

/** Vue publique du compte : sans passwordHash. */
public record UserResponse(
        String id,
        String email,
        String firstName,
        String lastName,
        String phone,
        String city,
        Role role,
        AccountStatus status,
        Instant createdAt) {

    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(),
                u.getPhone(), u.getCity(), u.getRole(), u.getStatus(), u.getCreatedAt());
    }
}
