package com.testhakni.backend.repository;

import java.util.List;
import java.util.Optional;

import com.testhakni.backend.model.AccountStatus;
import com.testhakni.backend.model.Role;
import com.testhakni.backend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRoleAndStatus(Role role, AccountStatus status);
}
