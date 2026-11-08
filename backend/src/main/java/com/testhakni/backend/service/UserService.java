package com.testhakni.backend.service;

import com.testhakni.backend.dto.ChangePasswordRequest;
import com.testhakni.backend.dto.UpdateProfileRequest;
import com.testhakni.backend.dto.UserResponse;
import com.testhakni.backend.exception.ApiException;
import com.testhakni.backend.model.User;
import com.testhakni.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Profil du compte connecte. AUTH-09 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse getMe(String userId) {
        return UserResponse.from(load(userId));
    }

    public UserResponse updateMe(String userId, UpdateProfileRequest req) {
        User user = load(userId);
        user.setFirstName(req.firstName().trim());
        user.setLastName(req.lastName().trim());
        user.setPhone(AuthService.trimToNull(req.phone()));
        user.setCity(AuthService.trimToNull(req.city()));
        return UserResponse.from(userRepository.save(user));
    }

    public void changePassword(String userId, ChangePasswordRequest req) {
        User user = load(userId);
        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw ApiException.badRequest("Mot de passe actuel incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    private User load(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Utilisateur introuvable"));
    }
}
