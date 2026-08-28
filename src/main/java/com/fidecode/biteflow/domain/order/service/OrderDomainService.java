package com.fidecode.biteflow.domain.order.service;

import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.exception.InvalidOrderTransitionException;
import com.fidecode.biteflow.domain.order.valueobject.OrderStatus;
import com.fidecode.biteflow.domain.order.valueobject.OrderType;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Domain Service — regras de transição de status do pedido. POJO puro, sem dependência de framework —
 * anotação @ApplicationScoped é puramente infraestrutural (ciclo de vida CDI) e não afeta a lógica de domínio.
 */
@ApplicationScoped
public class OrderDomainService {

    /**
     * Avança o status do pedido sequencialmente. ANALISE → PREPARO → EM_ROTA → FINALIZADO Balcão pode pular EM_ROTA →
     * FINALIZADO direto de PREPARO.
     */
    public void advanceStatus(Order order) {
        OrderStatus current = order.getStatus();

        if (current == OrderStatus.CANCELADO) {
            throw new InvalidOrderTransitionException("Pedido cancelado não pode avançar.");
        }
        if (current == OrderStatus.FINALIZADO) {
            throw new InvalidOrderTransitionException("Pedido já finalizado.");
        }

        OrderStatus next = switch (current) {
        case ANALISE -> OrderStatus.PREPARO;
        case PREPARO -> {
            // Balcão pula EM_ROTA e vai direto para FINALIZADO
            if (order.getType() == OrderType.BALCAO) {
                yield OrderStatus.FINALIZADO;
            }
            yield OrderStatus.EM_ROTA;
        }
        case EM_ROTA -> OrderStatus.FINALIZADO;
        default -> throw new InvalidOrderTransitionException("Transição não permitida a partir de " + current);
        };

        applyStatusChange(order, next);
    }

    /**
     * Move o pedido para uma coluna específica (drag & drop do Kanban).
     */
    public void moveToColumn(Order order, OrderStatus targetStatus) {
        OrderStatus current = order.getStatus();

        if (current == OrderStatus.CANCELADO) {
            throw new InvalidOrderTransitionException("Pedido cancelado não pode ser movido.");
        }
        if (targetStatus == OrderStatus.CANCELADO
                && (current == OrderStatus.EM_ROTA || current == OrderStatus.FINALIZADO)) {
            throw new InvalidOrderTransitionException("Pedido em rota ou finalizado não pode ser cancelado.");
        }
        if (current == OrderStatus.FINALIZADO) {
            throw new InvalidOrderTransitionException("Pedido finalizado não pode ser movido.");
        }

        // Validar: motoboy obrigatório para EM_ROTA em delivery
        if (targetStatus == OrderStatus.EM_ROTA && order.getType() == OrderType.DELIVERY
                && order.getDriverId() == null) {
            throw new InvalidOrderTransitionException("Motoboy obrigatório para mover pedido delivery para EM_ROTA.");
        }

        applyStatusChange(order, targetStatus);
    }

    /**
     * Atribui motoboy ao pedido via método de comportamento do aggregate.
     */
    public void assignDriver(Order order, UUID driverId, String driverName) {
        order.assignDriver(driverId, driverName);
    }

    private void applyStatusChange(Order order, OrderStatus newStatus) {
        // Validar motoboy obrigatório para EM_ROTA em delivery
        if (newStatus == OrderStatus.EM_ROTA && order.getType() == OrderType.DELIVERY && order.getDriverId() == null) {
            throw new InvalidOrderTransitionException("Motoboy obrigatório para mover pedido delivery para EM_ROTA.");
        }

        order.changeStatus(newStatus);
    }
}
