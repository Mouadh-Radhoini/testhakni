package com.testhakni.backend.shared.domain.exception;

/**
 * Nature d'une erreur metier, independante du protocole.
 * La couche presentation la traduit en code HTTP (voir GlobalExceptionHandler).
 */
public enum ErrorType {
    BAD_REQUEST,
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    CONFLICT
}
