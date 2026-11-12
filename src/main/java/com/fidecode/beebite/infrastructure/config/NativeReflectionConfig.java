package com.fidecode.beebite.infrastructure.config;

// Auth
import com.fidecode.beebite.application.auth.dto.AccessibleStoreResponse;
import com.fidecode.beebite.application.auth.dto.CreateUserRequest;
import com.fidecode.beebite.application.auth.dto.GrantUserStoreAccessRequest;
import com.fidecode.beebite.application.auth.dto.LoginRequest;
import com.fidecode.beebite.application.auth.dto.LoginResponse;
import com.fidecode.beebite.application.auth.dto.UpdateUserRequest;
import com.fidecode.beebite.application.auth.dto.UserResponse;

// Store
import com.fidecode.beebite.application.store.dto.StoreInfoRequest;
import com.fidecode.beebite.application.store.dto.StoreInfoResponse;

// Exception
import com.fidecode.beebite.presentation.exception.ErrorResponse;

// Catalog
import com.fidecode.beebite.application.catalog.dto.AdditionalRequest;
import com.fidecode.beebite.application.catalog.dto.AdditionalResponse;
import com.fidecode.beebite.application.catalog.dto.CategoryRequest;
import com.fidecode.beebite.application.catalog.dto.CategoryResponse;
import com.fidecode.beebite.application.catalog.dto.IngredientRequest;
import com.fidecode.beebite.application.catalog.dto.IngredientResponse;
import com.fidecode.beebite.application.catalog.dto.ModifierGroupRequest;
import com.fidecode.beebite.application.catalog.dto.ModifierGroupResponse;
import com.fidecode.beebite.application.catalog.dto.ProductAvailabilityRequest;
import com.fidecode.beebite.application.catalog.dto.ProductComboComponentRequest;
import com.fidecode.beebite.application.catalog.dto.ProductComboComponentResponse;
import com.fidecode.beebite.application.catalog.dto.ProductRequest;
import com.fidecode.beebite.application.catalog.dto.ProductResponse;

// Order
import com.fidecode.beebite.application.order.dto.AssignDriverRequest;
import com.fidecode.beebite.application.order.dto.CreateOrderItemDto;
import com.fidecode.beebite.application.order.dto.CreateOrderRequest;
import com.fidecode.beebite.application.order.dto.MoveOrderRequest;
import com.fidecode.beebite.application.order.dto.NfeResponse;
import com.fidecode.beebite.application.order.dto.OrderKanbanResponse;
import com.fidecode.beebite.application.order.dto.OrderResponse;
import com.fidecode.beebite.application.order.dto.PublicOrderReviewInfoResponse;
import com.fidecode.beebite.application.order.dto.UpdateOrderRequest;

// Customer
import com.fidecode.beebite.application.customer.dto.CustomerRequest;
import com.fidecode.beebite.application.customer.dto.CustomerResponse;

// Driver & Delivery
import com.fidecode.beebite.application.delivery.dto.DriverRequest;
import com.fidecode.beebite.application.delivery.dto.DriverResponse;
import com.fidecode.beebite.application.delivery.dto.SettlementReportResponse;

// Cash
import com.fidecode.beebite.application.cash.dto.CashMovementRequest;
import com.fidecode.beebite.application.cash.dto.CashMovementResponse;
import com.fidecode.beebite.application.cash.dto.CashRegisterRequest;
import com.fidecode.beebite.application.cash.dto.CashRegisterResponse;
import com.fidecode.beebite.application.cash.dto.CashSessionResponse;
import com.fidecode.beebite.application.cash.dto.CashSessionSummaryResponse;
import com.fidecode.beebite.application.cash.dto.CloseCashSessionRequest;
import com.fidecode.beebite.application.cash.dto.DriverCollectionSummaryDto;
import com.fidecode.beebite.application.cash.dto.OpenCashSessionRequest;

// Financial
import com.fidecode.beebite.application.financial.dto.FinancialRecordResponse;
import com.fidecode.beebite.application.financial.dto.GeneralCmvRequest;
import com.fidecode.beebite.application.financial.dto.GeneralCmvResponse;
import com.fidecode.beebite.application.financial.dto.PaymentMethodReportResponse;
import com.fidecode.beebite.application.financial.dto.ProductCmvResponse;
import com.fidecode.beebite.application.financial.dto.SalesByProductResponse;
import com.fidecode.beebite.application.financial.dto.SalesSummaryResponse;

// Payment
import com.fidecode.beebite.application.payment.dto.PaymentRequest;
import com.fidecode.beebite.application.payment.dto.PaymentResponse;

// KDS & Production
import com.fidecode.beebite.application.kds.dto.KdsSettingsRequest;
import com.fidecode.beebite.application.kds.dto.KdsSettingsResponse;
import com.fidecode.beebite.application.kds.dto.KdsStationRequest;
import com.fidecode.beebite.application.kds.dto.KdsStationResponse;
import com.fidecode.beebite.application.kds.dto.ProductionTaskResponse;
import com.fidecode.beebite.application.production.dto.ProductionSectorRequest;
import com.fidecode.beebite.application.production.dto.ProductionSectorResponse;

// Printing
import com.fidecode.beebite.application.printing.dto.PreviewReceiptRequest;
import com.fidecode.beebite.application.printing.dto.PrinterRequest;
import com.fidecode.beebite.application.printing.dto.PrinterResponse;
import com.fidecode.beebite.application.printing.dto.PrintOrderRequest;
import com.fidecode.beebite.application.printing.dto.PrintResultResponse;
import com.fidecode.beebite.application.printing.dto.PrintRuleRequest;
import com.fidecode.beebite.application.printing.dto.PrintRuleResponse;
import com.fidecode.beebite.application.printing.dto.PrintTemplateRequest;
import com.fidecode.beebite.application.printing.dto.PrintTemplateResponse;
import com.fidecode.beebite.application.printing.dto.ReceiptPreviewResponse;

// Messaging
import com.fidecode.beebite.application.messaging.dto.ConversationPatchRequest;
import com.fidecode.beebite.application.messaging.dto.ConversationResponse;
import com.fidecode.beebite.application.messaging.dto.ResolveConversationRequest;
import com.fidecode.beebite.application.messaging.dto.SendMessageRequest;

// Stock / Inventory
import com.fidecode.beebite.application.inventory.dto.StockMovementRequest;
import com.fidecode.beebite.application.inventory.dto.StockMovementResponse;

// Analytics & Dashboard
import com.fidecode.beebite.application.analytics.dto.AnalyticsOverviewResponse;
import com.fidecode.beebite.application.dashboard.dto.DashboardResponse;

// Integration
import com.fidecode.beebite.application.integration.dto.IfoodIntegrationRequest;
import com.fidecode.beebite.application.integration.dto.IntegrationResponse;
import com.fidecode.beebite.application.integration.dto.IntegrationsResponse;
import com.fidecode.beebite.application.integration.dto.WhatsappIntegrationRequest;

// Sync / Edge
import com.fidecode.beebite.application.sync.dto.EdgeSyncBatchRequest;
import com.fidecode.beebite.application.sync.dto.EdgeSyncBatchResponse;
import com.fidecode.beebite.application.sync.dto.EdgeSyncEventDto;
import com.fidecode.beebite.domain.printing.valueobject.ReceiptSections;

// Shared
import com.fidecode.beebite.application.shared.PageResponse;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Registra todos os DTOs de Request, Response e records serializados/desserializados via Jackson
 * para funcionamento 100% garantido em imagem nativa GraalVM/Mandrel.
 */
@RegisterForReflection(targets = {
        // Auth
        LoginRequest.class,
        LoginResponse.class,
        LoginResponse.UserSummary.class,
        AccessibleStoreResponse.class,
        CreateUserRequest.class,
        UpdateUserRequest.class,
        UserResponse.class,
        GrantUserStoreAccessRequest.class,

        // Store
        StoreInfoRequest.class,
        StoreInfoResponse.class,

        // Exception / Error
        ErrorResponse.class,

        // Catalog
        AdditionalRequest.class,
        AdditionalResponse.class,
        CategoryRequest.class,
        CategoryResponse.class,
        IngredientRequest.class,
        IngredientResponse.class,
        ModifierGroupRequest.class,
        ModifierGroupRequest.ModifierOptionDto.class,
        ModifierGroupResponse.class,
        ModifierGroupResponse.ModifierOptionResponse.class,
        ProductAvailabilityRequest.class,
        ProductComboComponentRequest.class,
        ProductComboComponentResponse.class,
        ProductRequest.class,
        ProductResponse.class,
        ProductResponse.ChannelPriceDto.class,
        ProductResponse.TechnicalSheetItemResponse.class,
        ProductResponse.RemovableItemResponse.class,

        // Order
        CreateOrderRequest.class,
        CreateOrderItemDto.class,
        UpdateOrderRequest.class,
        MoveOrderRequest.class,
        AssignDriverRequest.class,
        OrderResponse.class,
        OrderResponse.OrderItemResponse.class,
        OrderResponse.ModifierResponse.class,
        OrderResponse.RemovalResponse.class,
        OrderKanbanResponse.class,
        PublicOrderReviewInfoResponse.class,
        NfeResponse.class,

        // Customer
        CustomerRequest.class,
        CustomerResponse.class,

        // Driver & Delivery
        DriverRequest.class,
        DriverResponse.class,
        SettlementReportResponse.class,
        SettlementReportResponse.SettlementEntry.class,
        SettlementReportResponse.SettlementOrderDto.class,

        // Cash
        CashRegisterRequest.class,
        CashRegisterResponse.class,
        OpenCashSessionRequest.class,
        CloseCashSessionRequest.class,
        CashSessionResponse.class,
        CashSessionSummaryResponse.class,
        CashMovementRequest.class,
        CashMovementResponse.class,
        DriverCollectionSummaryDto.class,

        // Financial
        FinancialRecordResponse.class,
        GeneralCmvRequest.class,
        GeneralCmvResponse.class,
        PaymentMethodReportResponse.class,
        PaymentMethodReportResponse.MethodEntry.class,
        ProductCmvResponse.class,
        SalesByProductResponse.class,
        SalesByProductResponse.ProductSale.class,
        SalesSummaryResponse.class,

        // Payment
        PaymentRequest.class,
        PaymentResponse.class,

        // KDS & Production
        KdsSettingsRequest.class,
        KdsSettingsResponse.class,
        KdsStationRequest.class,
        KdsStationResponse.class,
        ProductionTaskResponse.class,
        ProductionSectorRequest.class,
        ProductionSectorResponse.class,

        // Printing
        PrinterRequest.class,
        PrinterResponse.class,
        PrintOrderRequest.class,
        PrintResultResponse.class,
        PrintRuleRequest.class,
        PrintRuleResponse.class,
        PrintTemplateRequest.class,
        PrintTemplateResponse.class,
        PreviewReceiptRequest.class,
        ReceiptPreviewResponse.class,

        // Messaging
        ConversationResponse.class,
        ConversationPatchRequest.class,
        SendMessageRequest.class,
        ResolveConversationRequest.class,

        // Stock
        StockMovementRequest.class,
        StockMovementResponse.class,

        // Analytics & Dashboard
        AnalyticsOverviewResponse.class,
        AnalyticsOverviewResponse.Breakdown.class,
        DashboardResponse.class,
        DashboardResponse.StatItem.class,
        DashboardResponse.RecentOrder.class,
        DashboardResponse.DriverStatus.class,

        // Integration
        IfoodIntegrationRequest.class,
        WhatsappIntegrationRequest.class,
        IntegrationResponse.class,
        IntegrationsResponse.class,

        // Sync / Edge
        ReceiptSections.class,
        EdgeSyncBatchRequest.class,
        EdgeSyncBatchResponse.class,
        EdgeSyncEventDto.class,

        // Shared
        PageResponse.class
})
public class NativeReflectionConfig {
}
