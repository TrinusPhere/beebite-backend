package com.fidecode.biteflow.application.order;

import com.fidecode.biteflow.application.order.dto.*;
import com.fidecode.biteflow.domain.catalog.entity.Ingredient;
import com.fidecode.biteflow.domain.catalog.entity.ModifierGroup;
import com.fidecode.biteflow.domain.catalog.entity.ModifierOption;
import com.fidecode.biteflow.domain.catalog.entity.Product;
import com.fidecode.biteflow.domain.catalog.exception.ProductNotFoundException;
import com.fidecode.biteflow.domain.catalog.repository.IngredientRepository;
import com.fidecode.biteflow.domain.catalog.repository.ModifierGroupRepository;
import com.fidecode.biteflow.domain.catalog.repository.ProductRepository;
import com.fidecode.biteflow.domain.order.entity.*;
import com.fidecode.biteflow.domain.order.exception.InvalidOrderTransitionException;
import com.fidecode.biteflow.domain.order.exception.OrderNotFoundException;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import com.fidecode.biteflow.domain.order.valueobject.*;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class UpdateOrderUseCase {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ModifierGroupRepository modifierGroupRepository;
    private final IngredientRepository ingredientRepository;

    @Inject
    public UpdateOrderUseCase(OrderRepository orderRepository, ProductRepository productRepository,
            ModifierGroupRepository modifierGroupRepository, IngredientRepository ingredientRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.modifierGroupRepository = modifierGroupRepository;
        this.ingredientRepository = ingredientRepository;
    }

    public OrderResponse update(UUID storeId, UUID orderId, UpdateOrderRequest request) {
        Order order = StoreGuard.enforce(
                orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId.toString())),
                storeId);

        if (!order.isEditable()) {
            throw new InvalidOrderTransitionException(
                    "Pedido " + order.getDisplayId() + " não pode ser editado no status " + order.getStatus() + ".");
        }

        if (request.type() != null) {
            order.changeType(OrderType.valueOf(request.type()));
        }
        if (request.paymentMethod() != null) {
            order.changePaymentMethod(CreateOrderUseCase.parsePaymentMethod(request.paymentMethod()));
        }
        if (request.customer() != null) {
            order.updateCustomerInfo(request.customer().name(), request.customer().address());
        }
        order.defineContext(request.operation(), request.channel(), request.serviceType(), request.source());

        if (request.newItems() != null && !request.newItems().isEmpty()) {
            List<UUID> productIds = new ArrayList<>();
            List<UUID> groupIds = new ArrayList<>();
            List<UUID> ingredientIds = new ArrayList<>();

            for (CreateOrderItemDto itemDto : request.newItems()) {
                productIds.add(UUID.fromString(itemDto.productId()));
                if (itemDto.modifiers() != null) {
                    for (var modDto : itemDto.modifiers()) {
                        groupIds.add(UUID.fromString(modDto.groupId()));
                    }
                }
                if (itemDto.removalIds() != null) {
                    for (String removalId : itemDto.removalIds()) {
                        ingredientIds.add(UUID.fromString(removalId));
                    }
                }
            }

            Map<UUID, Product> productCache = productRepository.findAllById(productIds).stream()
                    .collect(Collectors.toMap(Product::getId, p -> p));
            Map<UUID, ModifierGroup> groupCache = modifierGroupRepository.findAllById(groupIds).stream()
                    .collect(Collectors.toMap(ModifierGroup::getId, g -> g));
            Map<UUID, Ingredient> ingredientCache = ingredientRepository.findAllById(ingredientIds).stream()
                    .collect(Collectors.toMap(Ingredient::getId, i -> i));

            for (CreateOrderItemDto itemDto : request.newItems()) {
                UUID productId = UUID.fromString(itemDto.productId());
                Product product = productCache.get(productId);
                if (product == null)
                    throw new ProductNotFoundException(itemDto.productId());

                OrderItem item = new OrderItem(productId, product.getName(), itemDto.quantity(), product.getPrice(),
                        itemDto.observation());

                if (itemDto.modifiers() != null) {
                    for (var modDto : itemDto.modifiers()) {
                        UUID groupId = UUID.fromString(modDto.groupId());
                        UUID optionId = UUID.fromString(modDto.optionId());
                        ModifierGroup group = groupCache.get(groupId);
                        String groupName = group != null ? group.getName() : "Desconhecido";
                        String optionName = "Desconhecido";
                        BigDecimal optionPrice = BigDecimal.ZERO;
                        if (group != null && group.getOptions() != null) {
                            for (ModifierOption opt : group.getOptions()) {
                                if (opt.getId().equals(optionId)) {
                                    optionName = opt.getName();
                                    optionPrice = opt.getPrice();
                                    break;
                                }
                            }
                        }
                        item.addModifier(new OrderItemModifier(groupId, groupName, optionId, optionName, optionPrice));
                    }
                }

                if (itemDto.removalIds() != null) {
                    for (String removalId : itemDto.removalIds()) {
                        UUID ingredientId = UUID.fromString(removalId);
                        Ingredient ing = ingredientCache.get(ingredientId);
                        String ingredientName = ing != null ? ing.getName() : "Desconhecido";
                        item.addRemoval(new OrderItemRemoval(ingredientId, ingredientName));
                    }
                }

                order.addItem(item);
            }
        }

        order.recalculateTotals();
        Order saved = orderRepository.save(order);
        return OrderDtoMapper.toResponse(saved);
    }
}
