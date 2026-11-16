package com.trinusphere.beebite.presentation.rest;

import com.trinusphere.beebite.application.payment.ListOrderPaymentsUseCase;
import com.trinusphere.beebite.application.payment.RegisterPaymentUseCase;
import com.trinusphere.beebite.application.payment.dto.PaymentRequest;
import com.trinusphere.beebite.application.payment.dto.PaymentResponse;
import io.quarkus.security.PermissionsAllowed;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "GERENTE", "CAIXA"})
public class PaymentResource extends BaseResource {

    private final RegisterPaymentUseCase registerPaymentUseCase;
    private final ListOrderPaymentsUseCase listOrderPaymentsUseCase;

    @Inject
    public PaymentResource(RegisterPaymentUseCase registerPaymentUseCase,
                           ListOrderPaymentsUseCase listOrderPaymentsUseCase) {
        this.registerPaymentUseCase = registerPaymentUseCase;
        this.listOrderPaymentsUseCase = listOrderPaymentsUseCase;
    }

    @POST
    @PermissionsAllowed("caixa")
    public Response registerPayment(@Valid PaymentRequest request) {
        PaymentResponse response = registerPaymentUseCase.execute(getStoreId(), request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/orders/{orderId}")
    @PermissionsAllowed("caixa")
    public List<PaymentResponse> listByOrder(@PathParam("orderId") UUID orderId) {
        return listOrderPaymentsUseCase.execute(getStoreId(), orderId);
    }
}
