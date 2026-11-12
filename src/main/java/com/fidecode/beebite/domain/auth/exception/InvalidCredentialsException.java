package com.fidecode.beebite.domain.auth.exception;

import com.fidecode.beebite.domain.shared.exception.UnauthorizedException;

public class InvalidCredentialsException extends UnauthorizedException {
    public InvalidCredentialsException() {
        super("Credenciais inválidas.");
    }
}
