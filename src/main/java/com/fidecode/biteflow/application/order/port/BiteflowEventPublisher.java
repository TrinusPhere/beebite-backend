package com.fidecode.biteflow.application.order.port;

import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.messaging.entity.Conversation;
import com.fidecode.biteflow.domain.messaging.entity.ConversationMessage;

/**
 * Port — BiteflowEventPublisher. Abstrai a publicação de eventos de pedidos iFood no Pub/Sub. Implementado em
 * infrastructure por PubSubBiteflowEventPublisher.
 *
 * Regra: apenas pedidos com platform == IFOOD publicam eventos.
 */
public interface BiteflowEventPublisher {

    /** Publica evento de mudança de status no tópico biteflow-order-status-changed. */
    void publishStatusChanged(Order order);

    /**
     * Publica evento de cancelamento no tópico biteflow-order-cancelled.
     *
     * @param cancellationCode
     *            código de cancelamento do iFood
     * @param reason
     *            descrição do motivo
     */
    void publishCancelled(Order order, String cancellationCode, String reason);

    /** Publica evento de mensagem de chat enviada. */
    void publishChatMessageSent(Conversation conversation, ConversationMessage message);
}
