package com.fidecode.biteflow.application.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response — dados consolidados do painel (Dashboard).
 */
public record DashboardResponse(
    List<StatItem> stats,
    List<RecentOrder> recentOrders,
    List<DriverStatus> deliveryTeam,
    LocalDateTime lastUpdatedAt
) {

    public record StatItem(
        String name,
        BigDecimal value,
        String formattedValue,
        String changePercentage
    ) {}

    public record RecentOrder(
        String displayId,
        String customerName,
        String itemsSummary,
        BigDecimal totalValue,
        String status,
        LocalDateTime createdAt
    ) {}

    public record DriverStatus(
        UUID id,
        String name,
        String status
    ) {}
}
