package com.fidecode.beebite.application.financial.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response — ranking de vendas por produto.
 */
public record SalesByProductResponse(long startDate, long endDate, List<ProductSale> products) {
    public record ProductSale(String productName, int quantity, BigDecimal totalValue) {
    }
}
