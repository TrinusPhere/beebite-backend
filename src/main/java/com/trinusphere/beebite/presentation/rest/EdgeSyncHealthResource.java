package com.trinusphere.beebite.presentation.rest;

import com.trinusphere.beebite.application.sync.GetEdgeSyncHealthUseCase;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncHealthResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/sync/health")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "GERENTE", "CAIXA"})
public class EdgeSyncHealthResource extends BaseResource {

    @Inject
    GetEdgeSyncHealthUseCase getEdgeSyncHealthUseCase;

    @GET
    public Response getHealth() {
        EdgeSyncHealthResponse response = getEdgeSyncHealthUseCase.execute(getStoreId());
        return Response.ok(response).build();
    }
}

