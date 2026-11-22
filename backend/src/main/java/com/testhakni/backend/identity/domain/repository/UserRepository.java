package com.testhakni.backend.identity.domain.repository;

import java.util.List;
import java.util.Optional;

import com.testhakni.backend.identity.domain.model.AccountStatus;
import com.testhakni.backend.identity.domain.model.Role;
import com.testhakni.backend.identity.domain.model.User;

/** Port du domaine : acces aux comptes. Implemente dans infrastructure.persistence. */
public interface UserRepository {

    Optional<User> findById(String id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    User save(User user);

    void delete(User user);

    long count();

    /** Filtres optionnels (null = pas de filtre). */
    List<User> search(Role role, AccountStatus status);

    void updateStatus(String id, AccountStatus status);
}
