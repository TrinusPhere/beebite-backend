package com.fidecode.biteflow.presentation.rest;

import com.fidecode.biteflow.application.kds.GetKdsSettingsUseCase;
import com.fidecode.biteflow.application.kds.UpdateKdsSettingsUseCase;
import com.fidecode.biteflow.application.kds.dto.KdsSettingsRequest;
import com.fidecode.biteflow.application.kds.dto.KdsSettingsResponse;
import io.quarkus.security.PermissionsAllowed;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/kds/settings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "GERENTE"})
public class KdsSettingsResource extends BaseResource {

    private final GetKdsSettingsUseCase getUseCase;
    private final UpdateKdsSettingsUseCase updateUseCase;

    @Inject
    public KdsSettingsResource(GetKdsSettingsUseCase getUseCase,
                               UpdateKdsSettingsUseCase updateUseCase) {
        this.getUseCase = getUseCase;
        this.updateUseCase = updateUseCase;
    }

    @GET
    @PermissionsAllowed("kds")
    public KdsSettingsResponse get() {
        return getUseCase.execute(getStoreId());
    }

    @PUT
    @PermissionsAllowed("kds")
    public KdsSettingsResponse update(@Valid KdsSettingsRequest request) {
        return updateUseCase.execute(getStoreId(), request);
    }
}
