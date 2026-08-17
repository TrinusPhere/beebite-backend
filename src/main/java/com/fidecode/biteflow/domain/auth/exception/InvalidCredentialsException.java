package com.fidecode.biteflow.domain.auth.exception;

import com.fidecode.biteflow.domain.shared.exception.UnauthorizedException;

public class InvalidCredentialsException extends UnauthorizedException {
    public InvalidCredentialsException() {
        super("Credenciais inválidas.");
    }
}
