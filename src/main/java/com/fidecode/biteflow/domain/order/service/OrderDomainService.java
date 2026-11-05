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
     * Avança o status do pedido sequencialmente. ANALISE → PREPARO → EM_ROTA → FINALIZADO
     * Pedidos que não necessitam de motoboy (Balcão, Retirada no local, Salão, Consumo Local, Mesa, Comanda)
     * pulam EM_ROTA e vão direto para FINALIZADO.
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
            // Apenas pedidos delivery com entregador vão para EM_ROTA; retirada e salão vão direto para FINALIZADO
            if (!isDeliveryWithDriver(order)) {
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

        // Validar: motoboy obrigatório para EM_ROTA em delivery que requer entregador
        if (targetStatus == OrderStatus.EM_ROTA && isDeliveryWithDriver(order)
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

    /**
     * Determina se o pedido é de entrega a domicílio e exige motoboy.
     * Pedidos de balcão, retirada no local, salão, mesa ou comanda NÃO necessitam de entregador.
     */
    public static boolean isDeliveryWithDriver(Order order) {
        if (order == null || order.getType() == OrderType.BALCAO) {
            return false;
        }
        if ("SALAO".equalsIgnoreCase(order.getOperation()) || "BALCAO".equalsIgnoreCase(order.getOperation())) {
            return false;
        }
        String service = order.getServiceType();
        if ("RETIRADA".equalsIgnoreCase(service) || "PICKUP".equalsIgnoreCase(service)
                || "CONSUMO_LOCAL".equalsIgnoreCase(service) || "MESA".equalsIgnoreCase(service)
                || "COMANDA".equalsIgnoreCase(service)) {
            return false;
        }
        return order.getType() == OrderType.DELIVERY;
    }

    private void applyStatusChange(Order order, OrderStatus newStatus) {
        // Validar motoboy obrigatório para EM_ROTA apenas para delivery com entregador
        if (newStatus == OrderStatus.EM_ROTA && isDeliveryWithDriver(order) && order.getDriverId() == null) {
            throw new InvalidOrderTransitionException("Motoboy obrigatório para mover pedido delivery para EM_ROTA.");
        }

        order.changeStatus(newStatus);
    }
}
