package com.fidecode.biteflow.application.delivery;

import com.fidecode.biteflow.application.delivery.dto.DriverRequest;
import com.fidecode.biteflow.application.delivery.dto.DriverResponse;
import com.fidecode.biteflow.domain.delivery.entity.DeliveryDriver;
import com.fidecode.biteflow.domain.delivery.exception.DriverNotFoundException;
import com.fidecode.biteflow.domain.delivery.repository.DeliveryDriverRepository;
import com.fidecode.biteflow.domain.delivery.valueobject.DriverStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverUseCasesTest {

    @Mock DeliveryDriverRepository driverRepository;
    @InjectMocks DriverUseCases driverUseCases;

    private final UUID storeId = UUID.randomUUID();

    @Test
    void shouldCreateDriver() {
        DriverRequest request = new DriverRequest(
            "Carlos Silva", "(11) 98765-4321", "ATIVO",
            true, false, new BigDecimal("20.00"), true, new BigDecimal("5.00")
        );

        when(driverRepository.save(any())).thenAnswer(inv -> {
            DeliveryDriver d = inv.getArgument(0);
            d.setId(UUID.randomUUID());
            return d;
        });

        DriverResponse response = driverUseCases.create(storeId, request);

        assertNotNull(response.id());
        assertEquals("Carlos Silva", response.name());
        assertEquals("ATIVO", response.status());
        assertTrue(response.isFixed());
        assertEquals(0, new BigDecimal("20.00").compareTo(response.allowance()));
        verify(driverRepository).save(any());
    }

    @Test
    void shouldUpdateDriver() {
        UUID driverId = UUID.randomUUID();
        DeliveryDriver existing = new DeliveryDriver(storeId, "Old Name", "111");
        existing.setId(driverId);

        when(driverRepository.findById(driverId)).thenReturn(Optional.of(existing));
        when(driverRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DriverRequest request = new DriverRequest("New Name", "222", "INATIVO",
            false, true, BigDecimal.ZERO, false, BigDecimal.ZERO);

        DriverResponse response = driverUseCases.update(storeId, driverId, request);

        assertEquals("New Name", response.name());
        assertEquals("INATIVO", response.status());
    }

    @Test
    void shouldThrowOnUpdateNotFound() {
        UUID id = UUID.randomUUID();
        when(driverRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(DriverNotFoundException.class,
            () -> driverUseCases.update(storeId, id, new DriverRequest("X", null, null, null, null, null, null, null)));
    }

    @Test
    void shouldDeleteDriver() {
        UUID id = UUID.randomUUID();
        DeliveryDriver existing = new DeliveryDriver(storeId, "Test", "123");
        existing.setId(id);

        when(driverRepository.findById(id)).thenReturn(Optional.of(existing));

        driverUseCases.delete(storeId, id);

        verify(driverRepository).deleteById(id);
    }

    @Test
    void shouldThrowOnDeleteNotFound() {
        UUID id = UUID.randomUUID();
        when(driverRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(DriverNotFoundException.class, () -> driverUseCases.delete(storeId, id));
    }

    @Test
    void shouldListDriversByStore() {
        DeliveryDriver d1 = new DeliveryDriver(storeId, "A", "1");
        d1.setId(UUID.randomUUID());
        DeliveryDriver d2 = new DeliveryDriver(storeId, "B", "2");
        d2.setId(UUID.randomUUID());

        when(driverRepository.findAllByStoreId(storeId)).thenReturn(List.of(d1, d2));

        List<DriverResponse> result = driverUseCases.list(storeId);
        assertEquals(2, result.size());
    }

    @Test
    void shouldGetByIdOrThrow() {
        UUID id = UUID.randomUUID();
        DeliveryDriver d = new DeliveryDriver(storeId, "Carlos", "123");
        d.setId(id);

        when(driverRepository.findById(id)).thenReturn(Optional.of(d));

        DriverResponse result = driverUseCases.getById(storeId, id);
        assertEquals("Carlos", result.name());
    }

    @Test
    void shouldThrowOnGetByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(driverRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(DriverNotFoundException.class, () -> driverUseCases.getById(storeId, id));
    }
}
