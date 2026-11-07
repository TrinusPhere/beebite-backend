package com.fidecode.biteflow.presentation.rest;

import com.fidecode.biteflow.application.order.CreateOrderUseCase;
import com.fidecode.biteflow.application.order.GetOrderUseCase;
import com.fidecode.biteflow.application.order.dto.CreateOrderRequest;
import com.fidecode.biteflow.application.customer.GetCustomerByPhoneUseCase;
import com.fidecode.biteflow.application.customer.CreateCustomerUseCase;
import com.fidecode.biteflow.application.customer.dto.CustomerRequest;
import com.fidecode.biteflow.domain.store.repository.StoreRepository;
import com.fidecode.biteflow.domain.catalog.repository.ModifierGroupRepository;
import com.fidecode.biteflow.domain.catalog.repository.ProductCategoryRepository;
import com.fidecode.biteflow.domain.catalog.repository.ProductRepository;
import com.fidecode.biteflow.domain.store.exception.StoreNotFoundException;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Path("/api/v1/internal")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
public class InternalIntegrationResource {

    @Inject
    StoreRepository stores;
    @Inject
    ProductCategoryRepository categories;
    @Inject
    ProductRepository products;
    @Inject
    ModifierGroupRepository modifierGroups;
    @Inject
    CreateOrderUseCase createOrderUseCase;
    @Inject
    GetOrderUseCase getOrderUseCase;
    @Inject
    GetCustomerByPhoneUseCase getCustomerByPhoneUseCase;
    @Inject
    CreateCustomerUseCase createCustomerUseCase;

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
                "publicSlug", store.getPublicSlug() != null ? store.getPublicSlug() : "");
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
                .map(p -> {
                    var map = new HashMap<String, Object>();
                    map.put("id", p.getId());
                    map.put("name", p.getName());
                    map.put("categoryId", p.getCategoryId() != null ? p.getCategoryId() : "");
                    map.put("description", p.getDescription() != null ? p.getDescription() : "");
                    map.put("price", p.getPrice());
                    map.put("imageUrl", p.getImageUrl() != null ? p.getImageUrl() : "");
                    map.put("productType", p.getProductType().name());
                    map.put("saleUnit", p.getSaleUnit().name());
                    map.put("status", p.getStatus().name());
                    map.put("isAvailableDigitalMenu", p.isAvailableDigitalMenu());
                    map.put("modifierGroupIds", p.getModifierGroupIds() != null
                            ? p.getModifierGroupIds().stream().map(UUID::toString).toList()
                            : List.of());
                    return map;
                })
                .toList();
        return Response.ok(prods).build();
    }

    @GET
    @Path("/stores/{storeId}/modifier-groups")
    public Response getModifierGroups(@PathParam("storeId") UUID storeId) {
        var groups = modifierGroups.findAllByStoreId(storeId).stream()
                .map(g -> {
                    var map = new HashMap<String, Object>();
                    map.put("id", g.getId().toString());
                    map.put("name", g.getName());
                    map.put("type", g.getType().name());
                    map.put("required", g.isRequired());
                    map.put("minSelections", g.getMinSelections());
                    map.put("maxSelections", g.getMaxSelections());
                    map.put("icon", g.getIcon() != null ? g.getIcon() : "");
                    var opts = g.getOptions() != null ? g.getOptions().stream().map(o -> {
                        var oMap = new HashMap<String, Object>();
                        oMap.put("id", o.getId().toString());
                        oMap.put("name", o.getName());
                        oMap.put("price", o.getPrice());
                        oMap.put("linkedProductId", o.getLinkedProductId() != null ? o.getLinkedProductId().toString() : "");
                        return oMap;
                    }).toList() : List.of();
                    map.put("options", opts);
                    return map;
                })
                .toList();
        return Response.ok(groups).build();
    }

    @POST
    @Path("/orders")
    public Response createOrder(Map<String, Object> request) {
        UUID storeId = UUID.fromString((String) request.get("storeId"));

        List<Map<String, Object>> rawItems = (List<Map<String, Object>>) request.get("items");
        var items = rawItems.stream().map(i -> {
            var rawMods = (List<Map<String, Object>>) i.get("modifiers");
            List<com.fidecode.biteflow.application.order.dto.CreateOrderItemDto.ModifierDto> mods = rawMods != null
                ? rawMods.stream().map(m -> new com.fidecode.biteflow.application.order.dto.CreateOrderItemDto.ModifierDto(
                        (String) m.get("groupId"),
                        (String) m.get("optionId"))).toList()
                : List.of();
            var rawRemovals = (List<String>) i.get("removalIds");
            List<String> removals = rawRemovals != null ? rawRemovals : List.of();
            String obs = (String) i.get("notes");
            if (obs == null) {
                obs = (String) i.get("observation");
            }
            return new com.fidecode.biteflow.application.order.dto.CreateOrderItemDto(
                (String) i.get("productId"),
                (Integer) i.get("quantity"),
                obs,
                mods,
                removals);
        }).toList();

        var customerInfo = new CreateOrderRequest.CustomerInfo(
                null,
                (String) request.get("customerName"),
                (String) request.get("address"),
                (String) request.get("customerPhone"),
                (String) request.get("complement"),
                (String) request.get("reference"));

        String notesStr = (String) request.get("notes");
        if (notesStr == null)
            notesStr = "";

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
                request.get("cashChangeFor") != null ? new java.math.BigDecimal(request.get("cashChangeFor").toString())
                        : null,
                notesStr);

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
        Object rawStoreId = request.get("storeId");
        if (rawStoreId == null) {
            throw new IllegalArgumentException("storeId é obrigatório para cadastrar cliente.");
        }
        UUID storeId = rawStoreId instanceof UUID u ? u : UUID.fromString(rawStoreId.toString());
        CustomerRequest req = new CustomerRequest(
                (String) request.get("name"),
                (String) request.get("phone"),
                (String) request.get("email"),
                (String) request.get("zipCode"),
                (String) request.get("address"),
                (String) request.get("number"),
                (String) request.get("complement"),
                (String) request.get("neighborhood"));
        return Response.status(Response.Status.CREATED).entity(createCustomerUseCase.execute(storeId, req)).build();
    }
}
