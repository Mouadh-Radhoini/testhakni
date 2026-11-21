package com.testhakni.backend.identity.application;

import com.testhakni.backend.identity.application.dto.ChangePasswordRequest;
import com.testhakni.backend.identity.application.dto.UpdateProfileRequest;
import com.testhakni.backend.identity.application.dto.UserResponse;
import com.testhakni.backend.identity.application.port.PasswordHasher;
import com.testhakni.backend.identity.domain.model.User;
import com.testhakni.backend.identity.domain.repository.UserRepository;
import com.testhakni.backend.shared.domain.Text;
import com.testhakni.backend.shared.domain.exception.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cas d'usage : profil du compte connecte. AUTH-09 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public UserService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public UserResponse getMe(String userId) {
        return UserResponse.from(load(userId));
    }

    @Transactional
    public UserResponse updateMe(String userId, UpdateProfileRequest req) {
        User user = load(userId);
        user.setFirstName(req.firstName().trim());
        user.setLastName(req.lastName().trim());
        user.setPhone(Text.trimToNull(req.phone()));
        user.setCity(Text.trimToNull(req.city()));
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void changePassword(String userId, ChangePasswordRequest req) {
        User user = load(userId);
        if (!passwordHasher.matches(req.currentPassword(), user.getPasswordHash())) {
            throw DomainException.badRequest("Mot de passe actuel incorrect");
        }
        user.setPasswordHash(passwordHasher.hash(req.newPassword()));
        userRepository.save(user);
    }

    private User load(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> DomainException.notFound("Utilisateur introuvable"));
    }
}
