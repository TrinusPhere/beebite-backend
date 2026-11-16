package com.trinusphere.beebite.presentation.rest;

import com.trinusphere.beebite.application.order.GetPublicOrderReviewInfoUseCase;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.UUID;

@Path("/api/v1/public/order-reviews")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class PublicOrderReviewResource {
    @Inject
    GetPublicOrderReviewInfoUseCase useCase;

    @GET
    @Path("/{orderId}")
    public Response getInfo(@PathParam("orderId") UUID orderId) {
        return Response.ok(useCase.execute(orderId)).build();
    }
}
