package com.trinusphere.beebite.application.order.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderKanbanResponse(List<KanbanColumn> columns) {
    public record KanbanColumn(String id, String title, String icon, List<KanbanOrder> orders) {
    }

    public record KanbanOrder(String id, String displayId, String customerName, String customerPhone,
            String itemsSummary, BigDecimal totalValue, String status, String type, String operation, String channel,
            String serviceType, String source, String driverName, String platform, String externalOrderId,
            boolean nfeEmitted, String createdAt) {
    }
}
