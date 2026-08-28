package com.fidecode.biteflow.domain.delivery;

import com.fidecode.biteflow.domain.delivery.entity.DeliveryDriver;
import com.fidecode.biteflow.domain.delivery.entity.DeliverySettlement;
import com.fidecode.biteflow.domain.delivery.valueobject.DriverStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DeliveryDriverTest {

    @Test
    void shouldCreateDriverWithDefaults() {
        UUID storeId = UUID.randomUUID();
        DeliveryDriver driver = new DeliveryDriver(storeId, "Carlos Silva", "(11) 98765-4321");

        assertEquals("Carlos Silva", driver.getName());
        assertEquals("(11) 98765-4321", driver.getPhone());
        assertEquals(storeId, driver.getStoreId());
        assertEquals(DriverStatus.ATIVO, driver.getStatus());
        assertFalse(driver.isFixed());
        assertFalse(driver.isDaily());
        assertEquals(BigDecimal.ZERO, driver.getAllowance());
        assertFalse(driver.isHasFixedDeliveryValue());
        assertEquals(BigDecimal.ZERO, driver.getFixedDeliveryValue());
        assertEquals(0, driver.getTotalDeliveries());
        assertEquals(BigDecimal.ZERO, driver.getRating());
    }

    @Test
    void shouldIncrementDeliveries() {
        DeliveryDriver driver = new DeliveryDriver(UUID.randomUUID(), "Test", "123");
        assertEquals(0, driver.getTotalDeliveries());

        driver.incrementDeliveries();
        assertEquals(1, driver.getTotalDeliveries());

        driver.incrementDeliveries();
        assertEquals(2, driver.getTotalDeliveries());
    }

    @Test
    void shouldCalculateSettlementTotal() {
        // total = (deliveries × feePerDelivery) + allowance
        // total = (12 × 5.00) + 20.00 = 80.00
        DeliverySettlement settlement = new DeliverySettlement(UUID.randomUUID().toString(), "João Silva", "FIXO", 12,
                new BigDecimal("20.00"), new BigDecimal("5.00"));

        assertEquals(0, new BigDecimal("80.00").compareTo(settlement.getTotal()));
        assertEquals(12, settlement.getDeliveries());
        assertEquals("João Silva", settlement.getDriverName());
        assertEquals("FIXO", settlement.getDriverType());
    }

    @Test
    void shouldCalculateSettlementWithZeroDeliveries() {
        // total = (0 × 5.00) + 20.00 = 20.00 (only allowance)
        DeliverySettlement settlement = new DeliverySettlement(UUID.randomUUID().toString(), "Maria", "DIARISTA", 0,
                new BigDecimal("20.00"), new BigDecimal("5.00"));

        assertEquals(0, new BigDecimal("20.00").compareTo(settlement.getTotal()));
    }

    @Test
    void shouldCalculateSettlementWithZeroAllowance() {
        // total = (5 × 8.00) + 0 = 40.00
        DeliverySettlement settlement = new DeliverySettlement(UUID.randomUUID().toString(), "Pedro", "DIARISTA", 5,
                BigDecimal.ZERO, new BigDecimal("8.00"));

        assertEquals(0, new BigDecimal("40.00").compareTo(settlement.getTotal()));
    }
}
