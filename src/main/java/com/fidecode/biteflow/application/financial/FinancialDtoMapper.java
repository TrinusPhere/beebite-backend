package com.fidecode.biteflow.application.financial;

import com.fidecode.biteflow.domain.financial.entity.FinancialRecord;
import com.fidecode.biteflow.application.financial.dto.FinancialRecordResponse;

/**
 * Mapper compartilhado — converte FinancialRecord (domain) → FinancialRecordResponse (DTO).
 * Classe final com métodos static (R2 — DtoMapper pattern).
 */
public final class FinancialDtoMapper {

    private FinancialDtoMapper() {}

    public static FinancialRecordResponse toResponse(FinancialRecord record) {
        var productDtos = record.getProducts().stream()
            .map(p -> new FinancialRecordResponse.FinancialRecordProductDto(
                p.getProductId(),
                p.getProductName(),
                p.getQuantity(),
                p.getPrice()
            ))
            .toList();

        return new FinancialRecordResponse(
            record.getId(),
            record.getOrderId(),
            record.getRecordDate(),
            record.getRecordTime(),
            record.getTotalValue(),
            record.getPaymentMethod().name(),
            record.getStatus().name(),
            productDtos
        );
    }
}
