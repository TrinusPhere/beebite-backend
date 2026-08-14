package com.fidecode.biteflow.presentation.rest;

import com.fidecode.biteflow.application.auth.GetUserUseCase;
import com.fidecode.biteflow.application.auth.LoginUseCase;
import com.fidecode.biteflow.application.auth.dto.LoginRequest;
import com.fidecode.biteflow.application.auth.dto.LoginResponse;
import com.fidecode.biteflow.application.auth.dto.UserResponse;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.UUID;

@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject LoginUseCase loginUseCase;
    @Inject GetUserUseCase getUserUseCase;
    @Inject JsonWebToken jwt;

    /**
     * POST /api/v1/auth/login — público
     */
    @POST
    @Path("/login")
    @PermitAll
    public Response login(@Valid LoginRequest request) {
        LoginResponse response = loginUseCase.execute(request);
        return Response.ok(response).build();
    }

    /**
     * POST /api/v1/auth/logout — autenticado (stateless: apenas retorna 204)
     */
    @POST
    @Path("/logout")
    @Authenticated
    public Response logout() {
        // JWT é stateless; invalidação real requer blocklist (fase futura)
        return Response.noContent().build();
    }

    /**
     * GET /api/v1/auth/me — autenticado
     */
    @GET
    @Path("/me")
    @Authenticated
    public Response me(@Context SecurityContext ctx) {
        String userId = jwt.getClaim("userId");
        UserResponse user = getUserUseCase.execute(UUID.fromString(userId));
        return Response.ok(user).build();
    }
}
