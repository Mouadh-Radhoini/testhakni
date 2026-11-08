package com.testhakni.backend.controller;

import com.testhakni.backend.dto.ChangePasswordRequest;
import com.testhakni.backend.dto.UpdateProfileRequest;
import com.testhakni.backend.dto.UserResponse;
import com.testhakni.backend.security.AuthenticatedUser;
import com.testhakni.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
@Tag(name = "Mon compte", description = "Profil de l'utilisateur connecte (AUTH-09)")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Mon profil")
    public UserResponse me(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me) {
        return userService.getMe(me.id());
    }

    @PutMapping
    @Operation(summary = "Modifier mon profil (prenom, nom, telephone, ville)")
    public UserResponse update(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me,
                               @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateMe(me.id(), request);
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Changer mon mot de passe (AUTH-07)")
    public void changePassword(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me,
                               @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(me.id(), request);
    }
}
