package com.trinusphere.beebite.presentation.rest;

import com.trinusphere.beebite.application.auth.GetUserUseCase;
import com.trinusphere.beebite.application.auth.GrantUserStoreAccessUseCase;
import com.trinusphere.beebite.application.auth.ListAccessibleStoresUseCase;
import com.trinusphere.beebite.application.auth.LoginUseCase;
import com.trinusphere.beebite.application.auth.SwitchStoreUseCase;
import com.trinusphere.beebite.application.auth.UpdateMyProfileUseCase;
import com.trinusphere.beebite.application.auth.dto.AccessibleStoreResponse;
import com.trinusphere.beebite.application.auth.dto.GrantUserStoreAccessRequest;
import com.trinusphere.beebite.application.auth.dto.LoginRequest;
import com.trinusphere.beebite.application.auth.dto.LoginResponse;
import com.trinusphere.beebite.application.auth.dto.UpdateProfileRequest;
import com.trinusphere.beebite.application.auth.dto.UserResponse;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource extends BaseResource {

    @Inject
    LoginUseCase loginUseCase;
    @Inject
    GetUserUseCase getUserUseCase;
    @Inject
    ListAccessibleStoresUseCase listAccessibleStoresUseCase;
    @Inject
    SwitchStoreUseCase switchStoreUseCase;
    @Inject
    GrantUserStoreAccessUseCase grantUserStoreAccessUseCase;
    @Inject
    UpdateMyProfileUseCase updateMyProfileUseCase;

    @ConfigProperty(name = "biteflow.auth.cookie.secure", defaultValue = "true")
    boolean cookieSecure;

    @ConfigProperty(name = "biteflow.auth.cookie.same-site", defaultValue = "Lax")
    String cookieSameSite;

    private NewCookie createAuthCookie(String token) {
        return new NewCookie.Builder("biteflow_jwt")
                .value(token)
                .path("/")
                .maxAge(86400)
                .secure(cookieSecure)
                .httpOnly(true)
                .sameSite(resolveSameSite())
                .build();
    }

    private NewCookie createExpiredCookie() {
        return new NewCookie.Builder("biteflow_jwt")
                .path("/")
                .maxAge(0)
                .secure(cookieSecure)
                .httpOnly(true)
                .sameSite(resolveSameSite())
                .build();
    }

    private NewCookie.SameSite resolveSameSite() {
        if (cookieSameSite == null || cookieSameSite.isBlank()) {
            return NewCookie.SameSite.LAX;
        }
        return NewCookie.SameSite.valueOf(cookieSameSite.trim().toUpperCase());
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
        UserResponse user = getUserUseCase.execute(getStoreId(), getUserId());
        return Response.ok(user).build();
    }

    /**
     * PUT /api/v1/auth/me — atualiza nome e/ou senha do próprio usuário autenticado
     */
    @PUT
    @Path("/me")
    @Authenticated
    public Response updateMyProfile(@Valid UpdateProfileRequest request) {
        UserResponse updated = updateMyProfileUseCase.execute(getUserId(), request);
        return Response.ok(updated).build();
    }

    /**
     * GET /api/v1/auth/stores — lista todas as lojas vinculadas ao usuário autenticado
     */
    @GET
    @Path("/stores")
    @Authenticated
    public Response listAccessibleStores() {
        List<AccessibleStoreResponse> stores = listAccessibleStoresUseCase.execute(getUserId(), getStoreId());
        return Response.ok(stores).build();
    }

    /**
     * POST /api/v1/auth/switch-store/{storeId} — alterna o contexto de loja ativa em 1 clique sem deslogar
     */
    @POST
    @Path("/switch-store/{storeId}")
    @Authenticated
    public Response switchStore(@PathParam("storeId") UUID targetStoreId) {
        LoginResponse response = switchStoreUseCase.execute(getUserId(), targetStoreId);
        return Response.ok(response).cookie(createAuthCookie(response.token())).build();
    }

    /**
     * POST /api/v1/auth/store-access — concede acesso de um usuário existente a uma unidade adicional (ADMIN)
     */
    @POST
    @Path("/store-access")
    @RolesAllowed("ADMIN")
    public Response grantStoreAccess(@Valid GrantUserStoreAccessRequest request) {
        AccessibleStoreResponse granted = grantUserStoreAccessUseCase.execute(request);
        return Response.status(Response.Status.CREATED).entity(granted).build();
    }
}
