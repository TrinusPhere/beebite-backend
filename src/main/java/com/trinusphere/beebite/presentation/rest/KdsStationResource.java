package com.trinusphere.beebite.presentation.rest;

import com.trinusphere.beebite.application.kds.*;
import com.trinusphere.beebite.application.kds.dto.KdsStationRequest;
import com.trinusphere.beebite.application.kds.dto.KdsStationResponse;
import io.quarkus.security.PermissionsAllowed;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/kds/stations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "GERENTE"})
public class KdsStationResource extends BaseResource {

    private final CreateKdsStationUseCase createUseCase;
    private final UpdateKdsStationUseCase updateUseCase;
    private final DeleteKdsStationUseCase deleteUseCase;
    private final ListKdsStationsUseCase listUseCase;
    private final GetKdsStationUseCase getUseCase;

    @Inject
    public KdsStationResource(CreateKdsStationUseCase createUseCase,
                              UpdateKdsStationUseCase updateUseCase,
                              DeleteKdsStationUseCase deleteUseCase,
                              ListKdsStationsUseCase listUseCase,
                              GetKdsStationUseCase getUseCase) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.deleteUseCase = deleteUseCase;
        this.listUseCase = listUseCase;
        this.getUseCase = getUseCase;
    }

    @GET
    @PermissionsAllowed("kds")
    public List<KdsStationResponse> list() {
        return listUseCase.execute(getStoreId());
    }

    @GET
    @Path("/{id}")
    @PermissionsAllowed("kds")
    public KdsStationResponse getById(@PathParam("id") UUID id) {
        return getUseCase.execute(getStoreId(), id);
    }

    @POST
    @PermissionsAllowed("kds")
    public Response create(@Valid KdsStationRequest request) {
        KdsStationResponse response = createUseCase.execute(getStoreId(), request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @PermissionsAllowed("kds")
    public KdsStationResponse update(@PathParam("id") UUID id, @Valid KdsStationRequest request) {
        return updateUseCase.execute(getStoreId(), id, request);
    }

    @DELETE
    @Path("/{id}")
    @PermissionsAllowed("kds")
    public Response delete(@PathParam("id") UUID id) {
        deleteUseCase.execute(getStoreId(), id);
        return Response.noContent().build();
    }
}
