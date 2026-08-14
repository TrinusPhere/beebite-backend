package com.fidecode.biteflow.presentation.rest;

import com.fidecode.biteflow.application.auth.*;
import com.fidecode.biteflow.application.auth.dto.*;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class UserResource {

    @Inject ListUsersUseCase listUsersUseCase;
    @Inject GetUserUseCase getUserUseCase;
    @Inject CreateUserUseCase createUserUseCase;
    @Inject UpdateUserUseCase updateUserUseCase;
    @Inject DeleteUserUseCase deleteUserUseCase;
    @Inject JsonWebToken jwt;

    /**
     * GET /api/v1/users — permissão: usuarios
     */
    @GET
    public Response listUsers() {
        checkPermission("usuarios");
        List<UserResponse> users = listUsersUseCase.execute();
        return Response.ok(users).build();
    }

    /**
     * GET /api/v1/users/{id}
     */
    @GET
    @Path("/{id}")
    public Response getUser(@PathParam("id") UUID id) {
        checkPermission("usuarios");
        UserResponse user = getUserUseCase.execute(id);
        return Response.ok(user).build();
    }

    /**
     * POST /api/v1/users — permissão: usuarios
     */
    @POST
    public Response createUser(@Valid CreateUserRequest request) {
        checkPermission("usuarios");
        String storeIdClaim = jwt.getClaim("storeId");
        UUID storeId = storeIdClaim != null
            ? UUID.fromString(storeIdClaim)
            : UUID.fromString("00000000-0000-0000-0000-000000000001");
        UserResponse created = createUserUseCase.execute(storeId, request);
        return Response.created(URI.create("/api/v1/users/" + created.id()))
            .entity(created).build();
    }

    /**
     * PUT /api/v1/users/{id} — permissão: usuarios
     */
    @PUT
    @Path("/{id}")
    public Response updateUser(@PathParam("id") UUID id, @Valid UpdateUserRequest request) {
        checkPermission("usuarios");
        UserResponse updated = updateUserUseCase.execute(id, request);
        return Response.ok(updated).build();
    }

    /**
     * DELETE /api/v1/users/{id} — permissão: usuarios
     */
    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") UUID id) {
        checkPermission("usuarios");
        deleteUserUseCase.execute(id);
        return Response.noContent().build();
    }

    // ── helper: verifica permissão granular no claim do JWT ──────────────

    private void checkPermission(String required) {
        List<String> permissions = jwt.getClaim("permissions");
        boolean isAdmin = jwt.getGroups().contains("ADMIN");
        if (!isAdmin && (permissions == null || !permissions.contains(required))) {
            throw new jakarta.ws.rs.ForbiddenException("Permissão '" + required + "' necessária.");
        }
    }
}
