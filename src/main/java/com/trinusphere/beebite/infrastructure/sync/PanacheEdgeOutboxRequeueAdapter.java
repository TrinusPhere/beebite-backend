package com.trinusphere.beebite.infrastructure.sync;

import com.trinusphere.beebite.application.sync.EnqueueEdgeOutboxUseCase;
import com.trinusphere.beebite.application.sync.port.EdgeOutboxRequeuePort;
import com.trinusphere.beebite.domain.cash.repository.CashMovementRepository;
import com.trinusphere.beebite.domain.cash.repository.CashRegisterRepository;
import com.trinusphere.beebite.domain.cash.repository.CashSessionRepository;
import com.trinusphere.beebite.domain.catalog.repository.AdditionalRepository;
import com.trinusphere.beebite.domain.catalog.repository.IngredientRepository;
import com.trinusphere.beebite.domain.catalog.repository.ModifierGroupRepository;
import com.trinusphere.beebite.domain.catalog.repository.ProductCategoryRepository;
import com.trinusphere.beebite.domain.catalog.repository.ProductRepository;
import com.trinusphere.beebite.domain.financial.repository.FinancialRecordRepository;
import com.trinusphere.beebite.domain.order.repository.OrderRepository;
import com.trinusphere.beebite.domain.payment.repository.PaymentRepository;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import com.trinusphere.beebite.domain.sync.valueobject.ReconciliationEntityType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PanacheEdgeOutboxRequeueAdapter implements EdgeOutboxRequeuePort {

    @Inject
    StoreRepository storeRepository;
    @Inject
    OrderRepository orderRepository;
    @Inject
    PaymentRepository paymentRepository;
    @Inject
    FinancialRecordRepository financialRecordRepository;
    @Inject
    CashSessionRepository cashSessionRepository;
    @Inject
    CashMovementRepository cashMovementRepository;
    @Inject
    CashRegisterRepository cashRegisterRepository;
    @Inject
    ProductCategoryRepository categoryRepository;
    @Inject
    ModifierGroupRepository modifierGroupRepository;
    @Inject
    AdditionalRepository additionalRepository;
    @Inject
    IngredientRepository ingredientRepository;
    @Inject
    ProductRepository productRepository;
    @Inject
    EnqueueEdgeOutboxUseCase enqueueUseCase;
    @Inject
    EdgeSyncPayloadSerializer serializer;

    @Override
    @Transactional
    public int requeue(UUID storeId, ReconciliationEntityType type, List<UUID> aggregateIds) {
        if (storeId == null || type == null || aggregateIds == null || aggregateIds.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (UUID id : aggregateIds) {
            if (id != null) {
                count += requeueByEventType(storeId, type.getEventType(), id);
            }
        }
        return count;
    }

    @Override
    @Transactional
    public int requeueByEventType(UUID storeId, String eventType, UUID aggregateId) {
        if (storeId == null || eventType == null || aggregateId == null) {
            return 0;
        }

        Instant now = Instant.now();
        String payload = switch (eventType.trim().toUpperCase()) {
            case "ORDER_UPSERT" -> orderRepository.findById(aggregateId)
                    .filter(o -> storeId.equals(o.getStoreId()))
                    .map(serializer::serializeOrder)
                    .orElse(null);
            case "PAYMENT_UPSERT" -> paymentRepository.findById(aggregateId)
                    .filter(p -> storeId.equals(p.getStoreId()))
                    .map(serializer::serializePayment)
                    .orElse(null);
            case "FINANCIAL_RECORD_UPSERT" -> financialRecordRepository.findById(aggregateId)
                    .filter(r -> storeId.equals(r.getStoreId()))
                    .map(serializer::serializeFinancialRecord)
                    .orElse(null);
            case "CASH_SESSION_UPSERT" -> cashSessionRepository.findById(aggregateId)
                    .filter(s -> storeId.equals(s.getStoreId()))
                    .map(serializer::serializeCashSession)
                    .orElse(null);
            case "CASH_MOVEMENT_UPSERT" -> cashMovementRepository.findById(aggregateId)
                    .filter(m -> storeId.equals(m.getStoreId()))
                    .map(serializer::serializeCashMovement)
                    .orElse(null);
            case "CASH_REGISTER_UPSERT" -> cashRegisterRepository.findById(aggregateId)
                    .filter(r -> storeId.equals(r.getStoreId()))
                    .map(serializer::serializeCashRegister)
                    .orElse(null);
            case "STORE_STATUS_UPSERT" -> storeRepository.findById(storeId)
                    .map(serializer::serializeStoreStatus)
                    .orElse(null);
            case "CATALOG_CATEGORY_UPSERT" -> categoryRepository.findByIdInStore(aggregateId, storeId)
                    .map(c -> serializer.serializeCatalogCategory(c, now, null))
                    .orElse(null);
            case "CATALOG_MODIFIER_GROUP_UPSERT" -> modifierGroupRepository.findByIdInStore(aggregateId, storeId)
                    .map(g -> serializer.serializeCatalogModifierGroup(g, now, null))
                    .orElse(null);
            case "CATALOG_ADDITIONAL_UPSERT" -> additionalRepository.findByIdInStore(aggregateId, storeId)
                    .map(a -> serializer.serializeCatalogAdditional(a, now, null))
                    .orElse(null);
            case "CATALOG_INGREDIENT_UPSERT" -> ingredientRepository.findByIdInStore(aggregateId, storeId)
                    .map(i -> serializer.serializeCatalogIngredient(i, now, null))
                    .orElse(null);
            case "CATALOG_PRODUCT_UPSERT" -> productRepository.findByIdInStore(aggregateId, storeId)
                    .map(p -> serializer.serializeCatalogProduct(p, now, null))
                    .orElse(null);
            default -> null;
        };

        if (payload == null) {
            return 0;
        }

        return enqueueUseCase.execute(storeId, eventType.trim().toUpperCase(), aggregateId, payload).isPresent() ? 1 : 0;
    }
}

