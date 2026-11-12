package com.fidecode.beebite.presentation.rest;

import com.fidecode.beebite.application.printing.GetPrintTemplateUseCase;
import com.fidecode.beebite.application.printing.PreviewReceiptUseCase;
import com.fidecode.beebite.application.printing.UpdatePrintTemplateUseCase;
import com.fidecode.beebite.application.printing.dto.PreviewReceiptRequest;
import com.fidecode.beebite.application.printing.dto.PrintTemplateRequest;
import io.quarkus.security.PermissionsAllowed;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/print-templates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({ "ADMIN", "GERENTE" })
public class PrintTemplateResource extends BaseResource {

    @Inject
    GetPrintTemplateUseCase getTemplateUseCase;

    @Inject
    UpdatePrintTemplateUseCase updateTemplateUseCase;

    @Inject
    PreviewReceiptUseCase previewUseCase;

    @GET
    @Path("/{documentType}")
    @PermissionsAllowed("produtos")
    public Response getTemplate(@PathParam("documentType") String documentType) {
        return Response.ok(getTemplateUseCase.execute(getStoreId(), documentType)).build();
    }

    @PUT
    @Path("/{documentType}")
    @PermissionsAllowed("produtos")
    public Response updateTemplate(@PathParam("documentType") String documentType,
            @Valid PrintTemplateRequest request) {
        return Response.ok(updateTemplateUseCase.execute(getStoreId(), documentType, request)).build();
    }

    @POST
    @Path("/{documentType}/preview")
    @PermissionsAllowed("produtos")
    public Response preview(@PathParam("documentType") String documentType,
            @Valid PreviewReceiptRequest request) {
        return Response.ok(previewUseCase.execute(getStoreId(), documentType, request)).build();
    }
}
