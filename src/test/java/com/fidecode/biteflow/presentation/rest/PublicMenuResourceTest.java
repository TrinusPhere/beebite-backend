package com.fidecode.biteflow.presentation.rest;

import com.fidecode.biteflow.application.customer.GetCustomerByPhoneUseCase;
import com.fidecode.biteflow.application.customer.dto.CustomerResponse;
import com.fidecode.biteflow.application.order.GetOrderUseCase;
import com.fidecode.biteflow.application.order.dto.OrderResponse;
import com.fidecode.biteflow.application.publicmenu.CreatePublicOrderUseCase;
import com.fidecode.biteflow.application.publicmenu.GetPublicMenuUseCase;
import com.fidecode.biteflow.domain.store.entity.Store;
import com.fidecode.biteflow.domain.store.repository.StoreRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class PublicMenuResourceTest {

    @InjectMock
    StoreRepository storeRepository;

    @InjectMock
    GetOrderUseCase getOrderUseCase;

    @InjectMock
    GetPublicMenuUseCase getPublicMenuUseCase;

    @InjectMock
    CreatePublicOrderUseCase createPublicOrderUseCase;

    @InjectMock
    GetCustomerByPhoneUseCase getCustomerByPhoneUseCase;

    @Test
    public void testGetOrder() {
        String identifier = "biteflow-lanchonete";
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        
        Store mockStore = mock(Store.class);
        when(mockStore.getId()).thenReturn(storeId);
        
        OrderResponse mockResponse = new OrderResponse(
                orderId.toString(), "1234", "cust-id", "João", "Rua A", "123", "Casa", "Perto",
                "DELIVERY", "DELIVERY", "CARDAPIO_PROPRIO", "DELIVERY", "WEB", "PENDING",
                "PIX", null, "", null, null, "BITEFLOW", "ext-1", BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.TEN, false, List.of(), "2026-01-01T00:00:00Z", null
        );

        when(storeRepository.findByPublicIdentifier(identifier)).thenReturn(Optional.of(mockStore));
        when(getOrderUseCase.getById(storeId, orderId)).thenReturn(mockResponse);

        given()
          .pathParam("identifier", identifier)
          .pathParam("orderId", orderId)
        .when().get("/api/v1/public/stores/{identifier}/orders/{orderId}")
        .then()
           .statusCode(200)
           .body("id", is(orderId.toString()))
           .body("status", is("PENDING"))
           .body("customerName", is("João"));
    }

    @Test
    public void testGetCustomerByPhone() {
        String identifier = "biteflow-lanchonete";
        UUID storeId = UUID.randomUUID();
        String phone = "11999999999";
        
        Store mockStore = new Store();
        mockStore.setId(storeId);
        mockStore.setPublicIdentifier(identifier);
        
        CustomerResponse mockResponse = new CustomerResponse(
            UUID.randomUUID().toString(), "Maria", phone, "maria@email.com",
            "12345", "Rua A", "1", "Apto", "Centro", 0, "2026-01-01"
        );

        when(storeRepository.findByPublicIdentifier(identifier)).thenReturn(Optional.of(mockStore));
        when(getCustomerByPhoneUseCase.execute(storeId, phone)).thenReturn(mockResponse);

        given()
          .pathParam("identifier", identifier)
          .pathParam("phone", phone)
        .when().get("/api/v1/public/stores/{identifier}/customers/{phone}")
        .then()
           .statusCode(200)
           .body("name", is("Maria"))
           .body("phone", is(phone));
    }
}
