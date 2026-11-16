package com.trinusphere.beebite.presentation.rest;

import com.trinusphere.beebite.application.catalog.ListComboComponentsUseCase;
import com.trinusphere.beebite.application.catalog.ManageComboComponentsUseCase;
import com.trinusphere.beebite.application.catalog.dto.ProductComboComponentRequest;
import com.trinusphere.beebite.application.catalog.dto.ProductComboComponentResponse;
import io.quarkus.security.PermissionsAllowed;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/products/{id}/components")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "GERENTE"})
public class ProductComboComponentResource extends BaseResource {

    private final ListComboComponentsUseCase listUseCase;
    private final ManageComboComponentsUseCase manageUseCase;

    @Inject
    public ProductComboComponentResource(ListComboComponentsUseCase listUseCase,
                                         ManageComboComponentsUseCase manageUseCase) {
        this.listUseCase = listUseCase;
        this.manageUseCase = manageUseCase;
    }

    @GET
    @PermissionsAllowed("produtos")
    public List<ProductComboComponentResponse> list(@PathParam("id") UUID productId) {
        return listUseCase.execute(getStoreId(), productId);
    }

    @PUT
    @PermissionsAllowed("produtos")
    public List<ProductComboComponentResponse> manage(@PathParam("id") UUID productId,
                                                      @Valid List<ProductComboComponentRequest> requests) {
        return manageUseCase.execute(getStoreId(), productId, requests);
    }
}
