package com.testhakni.backend.shared.domain.exception;

/**
 * Exception metier levee par les couches domaine et application.
 * Elle ne connait pas HTTP : seul son {@link ErrorType} est transmis a la presentation.
 */
public class DomainException extends RuntimeException {

    private final ErrorType type;

    public DomainException(ErrorType type, String message) {
        super(message);
        this.type = type;
    }

    public ErrorType getType() {
        return type;
    }

    public static DomainException notFound(String message) {
        return new DomainException(ErrorType.NOT_FOUND, message);
    }

    public static DomainException conflict(String message) {
        return new DomainException(ErrorType.CONFLICT, message);
    }

    public static DomainException badRequest(String message) {
        return new DomainException(ErrorType.BAD_REQUEST, message);
    }

    public static DomainException unauthorized(String message) {
        return new DomainException(ErrorType.UNAUTHORIZED, message);
    }

    /** SEC-02 : l'utilisateur n'est pas proprietaire de la ressource. */
    public static DomainException forbidden(String message) {
        return new DomainException(ErrorType.FORBIDDEN, message);
    }
}
