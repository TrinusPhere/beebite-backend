package com.fidecode.biteflow.application.kds;

import com.fidecode.biteflow.application.kds.dto.ProductionTaskDtoMapper;
import com.fidecode.biteflow.application.kds.dto.ProductionTaskResponse;
import com.fidecode.biteflow.application.kds.port.KdsEventPublisher;
import com.fidecode.biteflow.domain.catalog.entity.Product;
import com.fidecode.biteflow.domain.catalog.valueobject.ProductType;
import com.fidecode.biteflow.domain.catalog.entity.ProductComboComponent;
import com.fidecode.biteflow.domain.catalog.repository.ProductComboComponentRepository;
import com.fidecode.biteflow.domain.catalog.repository.ProductRepository;
import com.fidecode.biteflow.domain.kds.entity.ProductionTask;
import com.fidecode.biteflow.domain.kds.repository.ProductionTaskRepository;
import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.entity.OrderItem;
import com.fidecode.biteflow.domain.order.exception.OrderNotFoundException;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class DispatchOrderToKdsUseCase {

    private final OrderRepository orderRepository;
    private final ProductionTaskRepository taskRepository;
    private final ProductRepository productRepository;
    private final ProductComboComponentRepository comboComponentRepository;
    private final KdsEventPublisher eventPublisher;

    @Inject
    public DispatchOrderToKdsUseCase(OrderRepository orderRepository,
                                     ProductionTaskRepository taskRepository,
                                     ProductRepository productRepository,
                                     ProductComboComponentRepository comboComponentRepository,
                                     KdsEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.taskRepository = taskRepository;
        this.productRepository = productRepository;
        this.comboComponentRepository = comboComponentRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<ProductionTaskResponse> execute(UUID storeId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId.toString()));
        StoreGuard.enforce(order, storeId);

        List<ProductionTask> existingTasks = taskRepository.findByOrderId(orderId);
        if (!existingTasks.isEmpty()) {
            return existingTasks.stream()
                    .map(ProductionTaskDtoMapper::toResponse)
                    .collect(Collectors.toList());
        }

        List<UUID> productIds = order.getItems().stream()
                .map(OrderItem::getProductId)
                .collect(Collectors.toList());

        Map<UUID, Product> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<ProductionTask> newTasks = new ArrayList<>();

        for (OrderItem item : order.getItems()) {
            Product product = productMap.get(item.getProductId());
            String modifiersSummary = buildModifiersSummary(item);

            if (product != null && product.getProductType() == ProductType.COMBO) {
                List<ProductComboComponent> components = comboComponentRepository.findByComboProductId(product.getId());
                if (!components.isEmpty()) {
                    List<UUID> compProductIds = components.stream()
                            .map(ProductComboComponent::getComponentProductId)
                            .collect(Collectors.toList());
                    Map<UUID, Product> compMap = productRepository.findAllById(compProductIds).stream()
                            .collect(Collectors.toMap(Product::getId, p -> p));

                    for (ProductComboComponent comp : components) {
                        Product compProduct = compMap.get(comp.getComponentProductId());
                        String compName = compProduct != null ? compProduct.getName() : "Item do Combo";
                        UUID compSector = compProduct != null ? compProduct.getProductionSectorId() : null;
                        int totalQty = item.getQuantity() * comp.getQuantity();

                        ProductionTask task = new ProductionTask(
                                storeId, orderId, item.getId(), compSector,
                                order.getDisplayId(),
                                order.getType() != null ? order.getType().name() : null,
                                order.getChannel(),
                                order.getCustomerName(),
                                compName, totalQty, item.getObservation(),
                                modifiersSummary, product.getName()
                        );
                        newTasks.add(task);
                    }
                    continue;
                }
            }

            UUID sectorId = product != null ? product.getProductionSectorId() : null;
            String itemName = product != null ? product.getName() : item.getProductName();

            ProductionTask task = new ProductionTask(
                    storeId, orderId, item.getId(), sectorId,
                    order.getDisplayId(),
                    order.getType() != null ? order.getType().name() : null,
                    order.getChannel(),
                    order.getCustomerName(),
                    itemName, item.getQuantity(), item.getObservation(),
                    modifiersSummary, null
            );
            newTasks.add(task);
        }

        List<ProductionTask> saved = taskRepository.saveAll(newTasks);
        List<ProductionTaskResponse> response = saved.stream()
                .map(ProductionTaskDtoMapper::toResponse)
                .collect(Collectors.toList());

        if (eventPublisher != null) {
            eventPublisher.publishTasksDispatched(storeId, response);
        }

        return response;
    }

    private String buildModifiersSummary(OrderItem item) {
        if (item.getModifiers() == null || item.getModifiers().isEmpty()) {
            return null;
        }
        return item.getModifiers().stream()
                .map(m -> m.getOptionName() != null ? m.getOptionName() : "")
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(", "));
    }
}
