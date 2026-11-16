package com.trinusphere.beebite.domain.auth.exception;

import com.trinusphere.beebite.domain.shared.exception.UnauthorizedException;

public class InvalidCredentialsException extends UnauthorizedException {
    public InvalidCredentialsException() {
        super("Credenciais inválidas.");
    }
}
