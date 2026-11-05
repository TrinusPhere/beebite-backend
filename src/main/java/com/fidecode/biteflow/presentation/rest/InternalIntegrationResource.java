package com.fidecode.biteflow.presentation.rest;

import com.fidecode.biteflow.application.order.CreateOrderUseCase;
import com.fidecode.biteflow.application.order.GetOrderUseCase;
import com.fidecode.biteflow.application.order.dto.CreateOrderRequest;
import com.fidecode.biteflow.application.customer.GetCustomerByPhoneUseCase;
import com.fidecode.biteflow.application.customer.CreateCustomerUseCase;
import com.fidecode.biteflow.application.customer.dto.CustomerRequest;
import com.fidecode.biteflow.domain.store.repository.StoreRepository;
import com.fidecode.biteflow.domain.catalog.repository.ProductCategoryRepository;
import com.fidecode.biteflow.domain.catalog.repository.ProductRepository;
import com.fidecode.biteflow.domain.store.exception.StoreNotFoundException;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.UUID;
import java.util.Map;
import java.util.List;

@Path("/api/v1/internal")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
public class InternalIntegrationResource {

    @Inject StoreRepository stores;
    @Inject ProductCategoryRepository categories;
    @Inject ProductRepository products;
    @Inject CreateOrderUseCase createOrderUseCase;
    @Inject GetOrderUseCase getOrderUseCase;
    @Inject GetCustomerByPhoneUseCase getCustomerByPhoneUseCase;
    @Inject CreateCustomerUseCase createCustomerUseCase;

    @GET
    @Path("/stores/by-slug/{slug}")
    public Response getStoreBySlug(@PathParam("slug") String slug) {
        var store = stores.findByPublicIdentifier(slug)
            .orElseThrow(() -> new StoreNotFoundException(slug));
        
        var dto = Map.of(
            "id", store.getId(),
            "name", store.getName(),
            "logoUrl", store.getLogoUrl() != null ? store.getLogoUrl() : "",
            "isOpen", store.isOpen(),
            "openingHours", store.getOpeningHours() != null ? store.getOpeningHours() : "",
            "address", store.getAddress() != null ? store.getAddress() : "",
            "publicSlug", store.getPublicSlug() != null ? store.getPublicSlug() : ""
        );
        return Response.ok(dto).build();
    }

    @GET
    @Path("/stores/{storeId}/categories")
    public Response getCategories(@PathParam("storeId") UUID storeId) {
        var cats = categories.findAllByStoreId(storeId).stream()
            .map(c -> Map.of("id", c.getId(), "name", c.getName()))
            .toList();
        return Response.ok(cats).build();
    }

    @GET
    @Path("/stores/{storeId}/products")
    public Response getProducts(@PathParam("storeId") UUID storeId) {
        var prods = products.findAllByStoreId(storeId).stream()
            .map(p -> Map.of(
                "id", p.getId(),
                "name", p.getName(),
                "categoryId", p.getCategoryId() != null ? p.getCategoryId() : "",
                "description", p.getDescription() != null ? p.getDescription() : "",
                "price", p.getPrice(),
                "imageUrl", p.getImageUrl() != null ? p.getImageUrl() : "",
                "productType", p.getProductType().name(),
                "saleUnit", p.getSaleUnit().name(),
                "status", p.getStatus().name(),
                "isAvailableDigitalMenu", p.isAvailableDigitalMenu()
            ))
            .toList();
        return Response.ok(prods).build();
    }

    @POST
    @Path("/orders")
    public Response createOrder(Map<String, Object> request) {
        UUID storeId = UUID.fromString((String) request.get("storeId"));
        
        List<Map<String, Object>> rawItems = (List<Map<String, Object>>) request.get("items");
        var items = rawItems.stream().map(i -> new com.fidecode.biteflow.application.order.dto.CreateOrderItemDto(
            (String) i.get("productId"),
            (Integer) i.get("quantity"),
            (String) i.get("notes"),
            List.of(), List.of()
        )).toList();
        
        var customerInfo = new CreateOrderRequest.CustomerInfo(
            null,
            (String) request.get("customerName"),
            (String) request.get("address"),
            (String) request.get("customerPhone"),
            (String) request.get("complement"),
            (String) request.get("reference")
        );
        
        String notesStr = (String) request.get("notes");
        if (notesStr == null) notesStr = "";
        
        String serviceType = (String) request.get("serviceType");
        if (serviceType == null || serviceType.isBlank()) {
            serviceType = "ENTREGA";
        }
        boolean isPickup = "RETIRADA".equalsIgnoreCase(serviceType) || "PICKUP".equalsIgnoreCase(serviceType);
        String orderType = isPickup ? "BALCAO" : "DELIVERY";
        String normalizedService = isPickup ? "RETIRADA" : "ENTREGA";
        
        var req = new CreateOrderRequest(
            orderType, customerInfo, (String) request.get("paymentMethod"), items,
            java.math.BigDecimal.ZERO, "DELIVERY", "CARDAPIO_PROPRIO",
            normalizedService, "WEB", 
            request.get("cashChangeFor") != null ? new java.math.BigDecimal(request.get("cashChangeFor").toString()) : null,
            notesStr
        );
        
        return Response.status(Response.Status.CREATED).entity(createOrderUseCase.create(storeId, req)).build();
    }

    @GET
    @Path("/orders/{storeId}/{orderId}")
    public Response getOrder(@PathParam("storeId") UUID storeId, @PathParam("orderId") UUID orderId) {
        return Response.ok(getOrderUseCase.getById(storeId, orderId)).build();
    }

    @GET
    @Path("/customers/{storeId}/by-phone/{phone}")
    public Response getCustomerByPhone(@PathParam("storeId") UUID storeId, @PathParam("phone") String phone) {
        return Response.ok(getCustomerByPhoneUseCase.execute(storeId, phone)).build();
    }

    @POST
    @Path("/customers")
    public Response createCustomer(Map<String, Object> request) {
        UUID storeId = UUID.fromString((String) request.get("storeId"));
        CustomerRequest req = new CustomerRequest(
            (String) request.get("name"),
            (String) request.get("phone"),
            (String) request.get("email"),
            (String) request.get("zipCode"),
            (String) request.get("address"),
            (String) request.get("number"),
            (String) request.get("complement"),
            (String) request.get("neighborhood")
        );
        return Response.status(Response.Status.CREATED).entity(createCustomerUseCase.execute(storeId, req)).build();
    }
}
