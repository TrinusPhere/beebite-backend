package com.trinusphere.beebite.application.cash.dto;

import java.math.BigDecimal;
import java.util.Map;

public record DriverCollectionSummaryDto(
        String driverId,
        String driverName,
        BigDecimal totalCollected,
        BigDecimal cashAmount,
        BigDecimal pixAmount,
        BigDecimal cardAmount,
        int deliveryCount
) {}
