package com.trinusphere.beebite.presentation.rest;

import com.trinusphere.beebite.application.printing.*;
import com.trinusphere.beebite.application.printing.dto.PrintRuleRequest;
import io.quarkus.security.PermissionsAllowed;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/api/v1/print-rules")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({ "ADMIN", "GERENTE" })
public class PrintRuleResource extends BaseResource {
    @Inject
    ListPrintRulesUseCase listUseCase;
    @Inject
    CreatePrintRuleUseCase createUseCase;

    @GET
    @PermissionsAllowed("produtos")
    public Response list() {
        return Response.ok(listUseCase.execute(getStoreId())).build();
    }

    @POST
    @PermissionsAllowed("produtos")
    public Response save(@Valid PrintRuleRequest r) {
        return Response.status(Response.Status.CREATED).entity(createUseCase.execute(getStoreId(), r)).build();
    }
}
