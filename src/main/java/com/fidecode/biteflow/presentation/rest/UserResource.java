package com.fidecode.biteflow.presentation.rest;

import com.fidecode.biteflow.application.auth.*;
import com.fidecode.biteflow.application.auth.dto.*;

import jakarta.annotation.security.RolesAllowed;
import io.quarkus.security.PermissionsAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)


@RolesAllowed("ADMIN")
public class UserResource extends BaseResource {

    @Inject ListUsersUseCase listUsersUseCase;
    @Inject GetUserUseCase getUserUseCase;
    @Inject CreateUserUseCase createUserUseCase;
    @Inject UpdateUserUseCase updateUserUseCase;
    @Inject DeleteUserUseCase deleteUserUseCase;

    /**
     * GET /api/v1/users — permissão: usuarios
     */
    @PermissionsAllowed("usuarios")
    @GET
    public Response listUsers() {
        List<UserResponse> users = listUsersUseCase.execute(getStoreId());
        return Response.ok(users).build();
    }

    /**
     * GET /api/v1/users/{id}
     */
    @PermissionsAllowed("usuarios")
    @GET
    @Path("/{id}")
    public Response getUser(@PathParam("id") UUID id) {
        UserResponse user = getUserUseCase.execute(getStoreId(), id);
        return Response.ok(user).build();
    }

    /**
     * POST /api/v1/users — permissão: usuarios
     */
    @PermissionsAllowed("usuarios")
    @POST
    public Response createUser(@Valid CreateUserRequest request) {
        UserResponse created = createUserUseCase.execute(getStoreId(), request);
        return Response.created(URI.create("/api/v1/users/" + created.id()))
            .entity(created).build();
    }

    /**
     * PUT /api/v1/users/{id} — permissão: usuarios
     */
    @PermissionsAllowed("usuarios")
    @PUT
    @Path("/{id}")
    public Response updateUser(@PathParam("id") UUID id, @Valid UpdateUserRequest request) {
        UserResponse updated = updateUserUseCase.execute(getStoreId(), id, request);
        return Response.ok(updated).build();
    }

    /**
     * DELETE /api/v1/users/{id} — permissão: usuarios
     */
    @PermissionsAllowed("usuarios")
    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") UUID id) {
        deleteUserUseCase.execute(getStoreId(), id);
        return Response.noContent().build();
    }
}
