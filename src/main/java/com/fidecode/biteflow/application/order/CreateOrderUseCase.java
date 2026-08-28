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
import com.fidecode.biteflow.domain.order.exception.InvalidPaymentMethodException;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import com.fidecode.biteflow.domain.order.valueobject.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ModifierGroupRepository modifierGroupRepository;
    private final IngredientRepository ingredientRepository;

    @Inject
    public CreateOrderUseCase(OrderRepository orderRepository, ProductRepository productRepository,
            ModifierGroupRepository modifierGroupRepository, IngredientRepository ingredientRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.modifierGroupRepository = modifierGroupRepository;
        this.ingredientRepository = ingredientRepository;
    }

    public OrderResponse create(UUID storeId, CreateOrderRequest request) {
        Order order = new Order(storeId, OrderType.valueOf(request.type()),
                parsePaymentMethod(request.paymentMethod()));
        order.defineContext(request.operation(), request.channel(), request.serviceType(), request.source());

        long displayIdNum = orderRepository.getNextDisplayIdSequence();
        order.setDisplayId(String.valueOf(displayIdNum));

        if (request.customer() != null) {
            if (request.customer().id() != null && !request.customer().id().isBlank()) {
                order.linkCustomer(UUID.fromString(request.customer().id()));
            }
            order.updateCustomerInfo(request.customer().name(), request.customer().address());
            order.updateCustomerContact(request.customer().phone(), request.customer().complement(),
                    request.customer().reference());
        }
        order.defineCashChangeFor(request.cashChangeFor());
        order.updateNotes(request.notes());

        if (request.deliveryCost() != null) {
            order.changeDeliveryCost(request.deliveryCost());
        }

        List<UUID> productIds = new ArrayList<>();
        List<UUID> groupIds = new ArrayList<>();
        List<UUID> ingredientIds = new ArrayList<>();

        for (CreateOrderItemDto itemDto : request.items()) {
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

        for (CreateOrderItemDto itemDto : request.items()) {
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

        order.recalculateTotals();
        Order saved = orderRepository.save(order);
        return OrderDtoMapper.toResponse(saved);
    }

    static PaymentMethod parsePaymentMethod(String value) {
        if (value == null)
            throw new InvalidPaymentMethodException("null");
        try {
            return switch (value.toUpperCase()) {
            case "CREDIT_CARD", "CREDITO" -> PaymentMethod.CREDITO;
            case "DEBIT_CARD", "DEBITO" -> PaymentMethod.DEBITO;
            case "CASH", "DINHEIRO" -> PaymentMethod.DINHEIRO;
            default -> PaymentMethod.valueOf(value.toUpperCase());
            };
        } catch (IllegalArgumentException ex) {
            throw new InvalidPaymentMethodException(value);
        }
    }

}
