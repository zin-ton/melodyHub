package com.backend.melodyHub.component;

import java.util.Optional;

public class TokenValidationResult {
    private final boolean valid;
    private final String errorMessage;

    public TokenValidationResult(boolean valid, String errorMessage) {
        this.valid = valid;
        this.errorMessage = errorMessage;
    }

    public static TokenValidationResult success() {
        return new TokenValidationResult(true, null);
    }

    public static TokenValidationResult error(String message) {
        return new TokenValidationResult(false, message);
    }

    public boolean isValid() {
        return valid;
    }

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }
}
