package com.fidecode.biteflow.application.cash.dto;

import java.math.BigDecimal;
import java.util.List;

public record CashSessionSummaryResponse(
        CashSessionResponse session,
        String registerName,
        String userName,
        BigDecimal initialAmount,
        BigDecimal cashSalesAmount,
        BigDecimal cardSalesAmount,
        BigDecimal pixSalesAmount,
        BigDecimal otherSalesAmount,
        BigDecimal totalSalesAmount,
        BigDecimal totalSuprimento,
        BigDecimal totalSangria,
        BigDecimal totalAjustes,
        BigDecimal calculatedCashBalance, // initial + cashSales + suprimento - sangria + ajustes
        List<DriverCollectionSummaryDto> driverCollections
) {}
