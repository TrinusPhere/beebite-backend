package com.fidecode.biteflow.presentation.rest;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Path("/api/v1/dev")
public class DevResource {

    @Inject
    EntityManager em;

    @POST
    @Path("/spread-orders")
    @Transactional
    public Response spreadOrders() {
        List<com.fidecode.biteflow.infrastructure.persistence.panache.entity.OrderEntity> orders = em.createQuery("SELECT o FROM OrderEntity o", com.fidecode.biteflow.infrastructure.persistence.panache.entity.OrderEntity.class).getResultList();
        for (com.fidecode.biteflow.infrastructure.persistence.panache.entity.OrderEntity order : orders) {
            int randomDaysBack = (int)(Math.random() * 7);
            LocalDateTime newDate = LocalDateTime.now().minusDays(randomDaysBack).minusHours((int)(Math.random() * 24)).minusMinutes((int)(Math.random() * 60));
            
            order.createdAt = newDate;
            order.updatedAt = newDate;
            if ("FINALIZADO".equals(order.status)) {
                order.finishedAt = newDate;
            }
            em.merge(order);
        }
        return Response.ok("Time travel completado para " + orders.size() + " pedidos").build();
    }
}
