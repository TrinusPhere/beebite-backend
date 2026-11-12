package com.fidecode.beebite.domain.delivery.entity;

import java.math.BigDecimal;

/**
 * Value object for settlement calculation. total = (deliveries × feePerDelivery) + allowance
 */
public class DeliverySettlement {

    private final String driverId;
    private final String driverName;
    private final String driverType;
    private final int deliveries;
    private final BigDecimal allowance;
    private final BigDecimal feePerDelivery;
    private final BigDecimal total;

    public DeliverySettlement(String driverId, String driverName, String driverType, int deliveries,
            BigDecimal allowance, BigDecimal feePerDelivery) {
        this.driverId = driverId;
        this.driverName = driverName;
        this.driverType = driverType;
        this.deliveries = deliveries;
        this.allowance = allowance;
        this.feePerDelivery = feePerDelivery;
        this.total = feePerDelivery.multiply(BigDecimal.valueOf(deliveries)).add(allowance);
    }

    public String getDriverId() {
        return driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getDriverType() {
        return driverType;
    }

    public int getDeliveries() {
        return deliveries;
    }

    public BigDecimal getAllowance() {
        return allowance;
    }

    public BigDecimal getFeePerDelivery() {
        return feePerDelivery;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
