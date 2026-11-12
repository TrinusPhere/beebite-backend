package com.fidecode.beebite.infrastructure.persistence.panache.mapper;

import com.fidecode.beebite.domain.financial.entity.FinancialRecord;
import com.fidecode.beebite.domain.financial.valueobject.FinancialRecordProduct;
import com.fidecode.beebite.domain.financial.valueobject.FinancialStatus;
import com.fidecode.beebite.domain.order.valueobject.PaymentMethod;
import com.fidecode.beebite.infrastructure.persistence.panache.entity.FinancialRecordEntity;
import com.fidecode.beebite.infrastructure.persistence.panache.entity.FinancialRecordProductEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper — converte entre domain FinancialRecord e JPA FinancialRecordEntity.
 */
public final class FinancialRecordMapper {

    private FinancialRecordMapper() {
    }

    public static FinancialRecord toDomain(FinancialRecordEntity entity) {
        List<FinancialRecordProduct> products = entity.products != null ? entity.products.stream().map(p -> {
            var product = new FinancialRecordProduct(p.productId, p.productName, p.quantity, p.price);
            product.setId(p.id);
            return product;
        }).toList() : new ArrayList<>();

        return FinancialRecord.reconstitute(entity.id, entity.storeId, entity.orderId, entity.recordDate,
                entity.recordTime, entity.totalValue, PaymentMethod.valueOf(entity.paymentMethod),
                FinancialStatus.valueOf(entity.status), products, entity.createdAt);
    }

    public static FinancialRecordEntity toEntity(FinancialRecord domain) {
        FinancialRecordEntity entity = new FinancialRecordEntity();
        entity.id = domain.getId();
        entity.storeId = domain.getStoreId();
        entity.orderId = domain.getOrderId();
        entity.recordDate = domain.getRecordDate();
        entity.recordTime = domain.getRecordTime();
        entity.totalValue = domain.getTotalValue();
        entity.paymentMethod = domain.getPaymentMethod().name();
        entity.status = domain.getStatus().name();
        entity.createdAt = domain.getCreatedAt();

        entity.products = domain.getProducts().stream().map(p -> {
            FinancialRecordProductEntity pe = new FinancialRecordProductEntity();
            pe.id = p.getId();
            pe.financialRecord = entity;
            pe.productId = p.getProductId();
            pe.productName = p.getProductName();
            pe.quantity = p.getQuantity();
            pe.price = p.getPrice();
            return pe;
        }).toList();

        return entity;
    }
}
