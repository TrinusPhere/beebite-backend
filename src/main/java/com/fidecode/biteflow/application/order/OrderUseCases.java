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
import com.fidecode.biteflow.domain.order.service.OrderDomainService;
import com.fidecode.biteflow.domain.order.valueobject.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrderUseCases {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ModifierGroupRepository modifierGroupRepository;
    private final IngredientRepository ingredientRepository;
    private final OrderDomainService orderDomainService;

    @Inject
    public OrderUseCases(OrderRepository orderRepository,
                         ProductRepository productRepository,
                         ModifierGroupRepository modifierGroupRepository,
                         IngredientRepository ingredientRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.modifierGroupRepository = modifierGroupRepository;
        this.ingredientRepository = ingredientRepository;
        this.orderDomainService = new OrderDomainService();
    }

    // For testing — allow injecting mock domain service
    OrderUseCases(OrderRepository orderRepository,
                  ProductRepository productRepository,
                  ModifierGroupRepository modifierGroupRepository,
                  IngredientRepository ingredientRepository,
                  OrderDomainService orderDomainService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.modifierGroupRepository = modifierGroupRepository;
        this.ingredientRepository = ingredientRepository;
        this.orderDomainService = orderDomainService;
    }

    public OrderResponse create(UUID storeId, CreateOrderRequest request) {
        OrderType type = OrderType.valueOf(request.type());
        PaymentMethod pm = PaymentMethod.valueOf(request.paymentMethod());

        Order order = new Order(storeId, type, pm);
        long seq = orderRepository.getNextDisplayIdSequence();
        order.setDisplayId("#" + seq);

        if (request.customer() != null) {
            order.setCustomerName(request.customer().name());
            order.setCustomerAddress(request.customer().address());
            if (request.customer().id() != null && !request.customer().id().isBlank()) {
                order.setCustomerId(UUID.fromString(request.customer().id()));
            }
        }

        if (request.deliveryCost() != null) {
            order.setDeliveryCost(request.deliveryCost());
        }

        // Build items with snapshot
        for (CreateOrderItemDto itemDto : request.items()) {
            UUID productId = UUID.fromString(itemDto.productId());
            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(itemDto.productId()));

            OrderItem item = new OrderItem(
                productId, product.getName(), itemDto.quantity(),
                product.getPrice(), itemDto.observation()
            );

            // Add modifiers
            if (itemDto.modifiers() != null) {
                for (var modDto : itemDto.modifiers()) {
                    UUID groupId = UUID.fromString(modDto.groupId());
                    UUID optionId = UUID.fromString(modDto.optionId());
                    ModifierGroup group = modifierGroupRepository.findById(groupId).orElse(null);
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

            // Add removals
            if (itemDto.removalIds() != null) {
                for (String removalId : itemDto.removalIds()) {
                    UUID ingredientId = UUID.fromString(removalId);
                    String ingredientName = ingredientRepository.findById(ingredientId)
                        .map(Ingredient::getName).orElse("Desconhecido");
                    item.addRemoval(new OrderItemRemoval(ingredientId, ingredientName));
                }
            }

            order.addItem(item);
        }

        order.recalculateTotals();
        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    public OrderResponse update(UUID orderId, UpdateOrderRequest request) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId.toString()));

        if (request.type() != null) {
            order.setType(OrderType.valueOf(request.type()));
        }
        if (request.paymentMethod() != null) {
            order.setPaymentMethod(PaymentMethod.valueOf(request.paymentMethod()));
        }
        if (request.customer() != null) {
            order.setCustomerName(request.customer().name());
            order.setCustomerAddress(request.customer().address());
        }

        // Add new items (preserve existing)
        if (request.additionalItems() != null) {
            for (CreateOrderItemDto itemDto : request.additionalItems()) {
                UUID productId = UUID.fromString(itemDto.productId());
                Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(itemDto.productId()));

                OrderItem item = new OrderItem(
                    productId, product.getName(), itemDto.quantity(),
                    product.getPrice(), itemDto.observation()
                );
                order.addItem(item);
            }
        }

        order.recalculateTotals();
        return toResponse(orderRepository.save(order));
    }

    public OrderResponse getById(UUID id) {
        return orderRepository.findById(id)
            .map(OrderUseCases::toResponse)
            .orElseThrow(() -> new OrderNotFoundException(id.toString()));
    }

    public void delete(UUID id) {
        orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id.toString()));
        orderRepository.deleteById(id);
    }

    public OrderResponse advanceStatus(UUID orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId.toString()));
        orderDomainService.advanceStatus(order);
        return toResponse(orderRepository.save(order));
    }

    public OrderResponse moveToColumn(UUID orderId, MoveOrderRequest request) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId.toString()));
        OrderStatus target = OrderStatus.valueOf(request.targetStatus());
        orderDomainService.moveToColumn(order, target);
        return toResponse(orderRepository.save(order));
    }

    public OrderResponse assignDriver(UUID orderId, AssignDriverRequest request) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId.toString()));
        UUID driverId = UUID.fromString(request.driverId());
        orderDomainService.assignDriver(order, driverId, request.driverName());
        return toResponse(orderRepository.save(order));
    }

    public void batchMove(List<String> orderIds, MoveOrderRequest request) {
        OrderStatus target = OrderStatus.valueOf(request.targetStatus());
        for (String id : orderIds) {
            UUID orderId = UUID.fromString(id);
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(id));
            orderDomainService.moveToColumn(order, target);
            orderRepository.save(order);
        }
    }

    public void batchAssignDriver(AssignDriverRequest request) {
        UUID driverId = UUID.fromString(request.driverId());
        if (request.orderIds() != null) {
            for (String id : request.orderIds()) {
                UUID orderId = UUID.fromString(id);
                Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException(id));
                orderDomainService.assignDriver(order, driverId, request.driverName());
                orderRepository.save(order);
            }
        }
    }

    public NfeResponse emitNfe(UUID orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId.toString()));
        order.markAsNfeEmitted();
        orderRepository.save(order);

        // Stub — retorna dados fictícios
        return new NfeResponse(
            orderId.toString(),
            String.format("%09d", new Random().nextInt(999999999)),
            "35260812345678000195550010001234561234567890",
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "AUTORIZADA"
        );
    }

    public OrderKanbanResponse listKanban(UUID storeId, String typeFilter) {
        List<Order> allOrders = orderRepository.findAllByStoreId(storeId);

        // Apply type filter if provided
        if (typeFilter != null && !typeFilter.isBlank()) {
            Set<String> types = Arrays.stream(typeFilter.split(","))
                .map(String::trim).map(String::toUpperCase)
                .collect(Collectors.toSet());
            allOrders = allOrders.stream()
                .filter(o -> types.contains(o.getType().name()))
                .toList();
        }

        Map<OrderStatus, List<Order>> grouped = allOrders.stream()
            .collect(Collectors.groupingBy(Order::getStatus));

        List<OrderKanbanResponse.KanbanColumn> columns = List.of(
            buildColumn("ANALISE", "Pedido em Análise", "Search", grouped.getOrDefault(OrderStatus.ANALISE, List.of())),
            buildColumn("PREPARO", "Em Preparo", "ChefHat", grouped.getOrDefault(OrderStatus.PREPARO, List.of())),
            buildColumn("EM_ROTA", "Saiu para Entrega", "Truck", grouped.getOrDefault(OrderStatus.EM_ROTA, List.of())),
            buildColumn("FINALIZADO", "Finalizado", "CheckCircle2", grouped.getOrDefault(OrderStatus.FINALIZADO, List.of()))
        );

        return new OrderKanbanResponse(columns);
    }

    private OrderKanbanResponse.KanbanColumn buildColumn(String id, String title, String icon, List<Order> orders) {
        List<OrderKanbanResponse.KanbanOrder> kanbanOrders = orders.stream()
            .map(o -> new OrderKanbanResponse.KanbanOrder(
                o.getId().toString(),
                o.getDisplayId(),
                o.getCustomerName(),
                buildItemsSummary(o),
                o.getTotalValue(),
                o.getType().name(),
                o.getDriverName(),
                o.getPlatform().name(),
                o.getExternalOrderId(),
                o.isNfeEmitted(),
                o.getCreatedAt() != null ? o.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null
            )).toList();
        return new OrderKanbanResponse.KanbanColumn(id, title, icon, kanbanOrders);
    }

    private String buildItemsSummary(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) return "";
        return order.getItems().stream()
            .map(i -> i.getQuantity() + "x " + i.getProductName())
            .collect(Collectors.joining(", "));
    }

    public static OrderResponse toResponse(Order o) {
        List<OrderResponse.OrderItemResponse> itemResponses = o.getItems() != null
            ? o.getItems().stream().map(OrderUseCases::toItemResponse).toList()
            : List.of();

        return new OrderResponse(
            o.getId() != null ? o.getId().toString() : null,
            o.getDisplayId(),
            o.getCustomerId() != null ? o.getCustomerId().toString() : null,
            o.getCustomerName(),
            o.getCustomerAddress(),
            o.getType().name(),
            o.getStatus().name(),
            o.getPaymentMethod().name(),
            o.getDriverId() != null ? o.getDriverId().toString() : null,
            o.getDriverName(),
            o.getPlatform().name(),
            o.getExternalOrderId(),
            o.getSubtotal(),
            o.getDeliveryCost(),
            o.getTotalValue(),
            o.isNfeEmitted(),
            itemResponses,
            o.getCreatedAt() != null ? o.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null,
            o.getFinishedAt() != null ? o.getFinishedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null
        );
    }

    private static OrderResponse.OrderItemResponse toItemResponse(OrderItem item) {
        List<OrderResponse.ModifierResponse> modResponses = item.getModifiers() != null
            ? item.getModifiers().stream().map(m -> new OrderResponse.ModifierResponse(
                m.getId() != null ? m.getId().toString() : null,
                m.getGroupId() != null ? m.getGroupId().toString() : null,
                m.getGroupName(),
                m.getOptionId() != null ? m.getOptionId().toString() : null,
                m.getOptionName(),
                m.getPrice()
            )).toList() : List.of();

        List<OrderResponse.RemovalResponse> remResponses = item.getRemovals() != null
            ? item.getRemovals().stream().map(r -> new OrderResponse.RemovalResponse(
                r.getId() != null ? r.getId().toString() : null,
                r.getIngredientId() != null ? r.getIngredientId().toString() : null,
                r.getIngredientName()
            )).toList() : List.of();

        return new OrderResponse.OrderItemResponse(
            item.getId() != null ? item.getId().toString() : null,
            item.getProductId() != null ? item.getProductId().toString() : null,
            item.getProductName(),
            item.getQuantity(),
            item.getUnitPrice(),
            item.getExtraPrice(),
            item.getTotalPrice(),
            item.getObservation(),
            modResponses,
            remResponses
        );
    }
}
