package com.fidecode.biteflow.presentation.exception;

import com.fidecode.biteflow.domain.auth.exception.InvalidCredentialsException;
import com.fidecode.biteflow.domain.auth.exception.UserAlreadyExistsException;
import com.fidecode.biteflow.domain.auth.exception.UserNotFoundException;
import com.fidecode.biteflow.domain.store.exception.StoreNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.stream.Collectors;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Exception exception) {
        String path = uriInfo != null ? uriInfo.getPath() : "";

        if (exception instanceof InvalidCredentialsException ex) {
            return Response.status(401)
                .entity(ErrorResponse.of(401, "Unauthorized", ex.getMessage(), path))
                .build();
        }

        if (exception instanceof UserNotFoundException ex) {
            return Response.status(404)
                .entity(ErrorResponse.of(404, "Not Found", ex.getMessage(), path))
                .build();
        }

        if (exception instanceof StoreNotFoundException ex) {
            return Response.status(404)
                .entity(ErrorResponse.of(404, "Not Found", ex.getMessage(), path))
                .build();
        }

        if (exception instanceof UserAlreadyExistsException ex) {
            return Response.status(409)
                .entity(ErrorResponse.of(409, "Conflict", ex.getMessage(), path))
                .build();
        }

        if (exception instanceof ConstraintViolationException ex) {
            String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
            return Response.status(400)
                .entity(ErrorResponse.of(400, "Bad Request", message, path))
                .build();
        }

        if (exception instanceof IllegalArgumentException ex) {
            return Response.status(400)
                .entity(ErrorResponse.of(400, "Bad Request", ex.getMessage(), path))
                .build();
        }

        if (exception instanceof WebApplicationException ex) {
            int status = ex.getResponse().getStatus();
            return Response.status(status)
                .entity(ErrorResponse.of(status, ex.getResponse().getStatusInfo().getReasonPhrase(), ex.getMessage(), path))
                .build();
        }

        // Fallback genérico — 500
        return Response.status(500)
            .entity(ErrorResponse.of(500, "Internal Server Error",
                "Erro interno. Tente novamente mais tarde.", path))
            .build();
    }
}
