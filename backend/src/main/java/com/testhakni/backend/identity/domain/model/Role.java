package com.testhakni.backend.identity.domain.model;

/** Roles de la plateforme. Spring Security les expose sous la forme ROLE_CUSTOMER, etc. */
public enum Role {
    CUSTOMER,
    PROVIDER,
    ADMIN
}
