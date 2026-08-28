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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.NewCookie;

import java.util.UUID;

@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource extends BaseResource {

    @Inject LoginUseCase loginUseCase;
    @Inject GetUserUseCase getUserUseCase;

    private NewCookie createAuthCookie(String token) {
        // secure = false for local development. We set HttpOnly = true.
        return new NewCookie("biteflow_jwt", token, "/", null, null, 86400, false, true);
    }

    private NewCookie createExpiredCookie() {
        return new NewCookie("biteflow_jwt", null, "/", null, null, 0, false, true);
    }

    /**
     * POST /api/v1/auth/login — público
     */
    @POST
    @Path("/login")
    @PermitAll
    public Response login(@Valid LoginRequest request) {
        LoginResponse response = loginUseCase.execute(request);
        return Response.ok(response).cookie(createAuthCookie(response.token())).build();
    }

    /**
     * POST /api/v1/auth/logout — público (para garantir que o cookie é limpo mesmo expirado)
     */
    @POST
    @Path("/logout")
    @PermitAll
    public Response logout() {
        return Response.noContent().cookie(createExpiredCookie()).build();
    }

    /**
     * GET /api/v1/auth/me — autenticado
     */
    @GET
    @Path("/me")
    @Authenticated
    public Response me() {
        Object rawUserId = jwt.getClaim("userId");
        String userId = rawUserId.toString().replace("\"", "");
        UserResponse user = getUserUseCase.execute(getStoreId(), UUID.fromString(userId));
        return Response.ok(user).build();
    }
}
