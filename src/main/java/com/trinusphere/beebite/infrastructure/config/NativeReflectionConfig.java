package com.trinusphere.beebite.infrastructure.config;

// Auth
import com.trinusphere.beebite.application.auth.dto.AccessibleStoreResponse;
import com.trinusphere.beebite.application.auth.dto.CreateUserRequest;
import com.trinusphere.beebite.application.auth.dto.GrantUserStoreAccessRequest;
import com.trinusphere.beebite.application.auth.dto.LoginRequest;
import com.trinusphere.beebite.application.auth.dto.LoginResponse;
import com.trinusphere.beebite.application.auth.dto.UpdateUserRequest;
import com.trinusphere.beebite.application.auth.dto.UserResponse;

// Store
import com.trinusphere.beebite.application.store.dto.StoreInfoRequest;
import com.trinusphere.beebite.application.store.dto.StoreInfoResponse;

// Exception
import com.trinusphere.beebite.presentation.exception.ErrorResponse;

// Catalog
import com.trinusphere.beebite.application.catalog.dto.AdditionalRequest;
import com.trinusphere.beebite.application.catalog.dto.AdditionalResponse;
import com.trinusphere.beebite.application.catalog.dto.CategoryRequest;
import com.trinusphere.beebite.application.catalog.dto.CategoryResponse;
import com.trinusphere.beebite.application.catalog.dto.IngredientRequest;
import com.trinusphere.beebite.application.catalog.dto.IngredientResponse;
import com.trinusphere.beebite.application.catalog.dto.ModifierGroupRequest;
import com.trinusphere.beebite.application.catalog.dto.ModifierGroupResponse;
import com.trinusphere.beebite.application.catalog.dto.ProductAvailabilityRequest;
import com.trinusphere.beebite.application.catalog.dto.ProductComboComponentRequest;
import com.trinusphere.beebite.application.catalog.dto.ProductComboComponentResponse;
import com.trinusphere.beebite.application.catalog.dto.ProductRequest;
import com.trinusphere.beebite.application.catalog.dto.ProductResponse;

// Order
import com.trinusphere.beebite.application.order.dto.AssignDriverRequest;
import com.trinusphere.beebite.application.order.dto.CreateOrderItemDto;
import com.trinusphere.beebite.application.order.dto.CreateOrderRequest;
import com.trinusphere.beebite.application.order.dto.MoveOrderRequest;
import com.trinusphere.beebite.application.order.dto.NfeResponse;
import com.trinusphere.beebite.application.order.dto.OrderKanbanResponse;
import com.trinusphere.beebite.application.order.dto.OrderResponse;
import com.trinusphere.beebite.application.order.dto.PublicOrderReviewInfoResponse;
import com.trinusphere.beebite.application.order.dto.UpdateOrderRequest;

// Customer
import com.trinusphere.beebite.application.customer.dto.CustomerRequest;
import com.trinusphere.beebite.application.customer.dto.CustomerResponse;

// Driver & Delivery
import com.trinusphere.beebite.application.delivery.dto.DriverRequest;
import com.trinusphere.beebite.application.delivery.dto.DriverResponse;
import com.trinusphere.beebite.application.delivery.dto.SettlementReportResponse;

// Cash
import com.trinusphere.beebite.application.cash.dto.CashMovementRequest;
import com.trinusphere.beebite.application.cash.dto.CashMovementResponse;
import com.trinusphere.beebite.application.cash.dto.CashRegisterRequest;
import com.trinusphere.beebite.application.cash.dto.CashRegisterResponse;
import com.trinusphere.beebite.application.cash.dto.CashSessionResponse;
import com.trinusphere.beebite.application.cash.dto.CashSessionSummaryResponse;
import com.trinusphere.beebite.application.cash.dto.CloseCashSessionRequest;
import com.trinusphere.beebite.application.cash.dto.DriverCollectionSummaryDto;
import com.trinusphere.beebite.application.cash.dto.OpenCashSessionRequest;

// Financial
import com.trinusphere.beebite.application.financial.dto.FinancialRecordResponse;
import com.trinusphere.beebite.application.financial.dto.GeneralCmvRequest;
import com.trinusphere.beebite.application.financial.dto.GeneralCmvResponse;
import com.trinusphere.beebite.application.financial.dto.PaymentMethodReportResponse;
import com.trinusphere.beebite.application.financial.dto.ProductCmvResponse;
import com.trinusphere.beebite.application.financial.dto.SalesByProductResponse;
import com.trinusphere.beebite.application.financial.dto.SalesSummaryResponse;

// Payment
import com.trinusphere.beebite.application.payment.dto.PaymentRequest;
import com.trinusphere.beebite.application.payment.dto.PaymentResponse;

// KDS & Production
import com.trinusphere.beebite.application.kds.dto.KdsSettingsRequest;
import com.trinusphere.beebite.application.kds.dto.KdsSettingsResponse;
import com.trinusphere.beebite.application.kds.dto.KdsStationRequest;
import com.trinusphere.beebite.application.kds.dto.KdsStationResponse;
import com.trinusphere.beebite.application.kds.dto.ProductionTaskResponse;
import com.trinusphere.beebite.application.production.dto.ProductionSectorRequest;
import com.trinusphere.beebite.application.production.dto.ProductionSectorResponse;

// Printing
import com.trinusphere.beebite.application.printing.dto.PreviewReceiptRequest;
import com.trinusphere.beebite.application.printing.dto.PrinterRequest;
import com.trinusphere.beebite.application.printing.dto.PrinterResponse;
import com.trinusphere.beebite.application.printing.dto.PrintOrderRequest;
import com.trinusphere.beebite.application.printing.dto.PrintResultResponse;
import com.trinusphere.beebite.application.printing.dto.PrintRuleRequest;
import com.trinusphere.beebite.application.printing.dto.PrintRuleResponse;
import com.trinusphere.beebite.application.printing.dto.PrintTemplateRequest;
import com.trinusphere.beebite.application.printing.dto.PrintTemplateResponse;
import com.trinusphere.beebite.application.printing.dto.ReceiptPreviewResponse;

// Messaging
import com.trinusphere.beebite.application.messaging.dto.ConversationPatchRequest;
import com.trinusphere.beebite.application.messaging.dto.ConversationResponse;
import com.trinusphere.beebite.application.messaging.dto.ResolveConversationRequest;
import com.trinusphere.beebite.application.messaging.dto.SendMessageRequest;

// Stock / Inventory
import com.trinusphere.beebite.application.inventory.dto.StockMovementRequest;
import com.trinusphere.beebite.application.inventory.dto.StockMovementResponse;

// Analytics & Dashboard
import com.trinusphere.beebite.application.analytics.dto.AnalyticsOverviewResponse;
import com.trinusphere.beebite.application.dashboard.dto.DashboardResponse;

// Integration
import com.trinusphere.beebite.application.integration.dto.IfoodIntegrationRequest;
import com.trinusphere.beebite.application.integration.dto.IntegrationResponse;
import com.trinusphere.beebite.application.integration.dto.IntegrationsResponse;
import com.trinusphere.beebite.application.integration.dto.WhatsappIntegrationRequest;

// Sync / Edge
import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchRequest;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchResponse;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncEventDto;
import com.trinusphere.beebite.domain.printing.valueobject.ReceiptSections;

// Platform / Trinusphere Backoffice
import com.trinusphere.beebite.application.platform.dto.ChangeTenantStatusRequest;
import com.trinusphere.beebite.application.platform.dto.CreateStoreForTenantRequest;
import com.trinusphere.beebite.application.platform.dto.MarkInvoicePaidRequest;
import com.trinusphere.beebite.application.platform.dto.OnboardClientRequest;
import com.trinusphere.beebite.application.platform.dto.OnboardClientResponse;
import com.trinusphere.beebite.application.platform.dto.PlatformOverviewResponse;
import com.trinusphere.beebite.application.platform.dto.SaasContractResponse;
import com.trinusphere.beebite.application.platform.dto.SaasInvoiceResponse;
import com.trinusphere.beebite.application.platform.dto.TenantAccountResponse;

// Shared
import com.trinusphere.beebite.application.shared.PageResponse;

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

        // Platform / Trinusphere Backoffice
        OnboardClientRequest.class,
        OnboardClientRequest.StoreSetupDto.class,
        OnboardClientRequest.ContractSetupDto.class,
        OnboardClientResponse.class,
        OnboardClientResponse.ProvisionedStoreDto.class,
        TenantAccountResponse.class,
        TenantAccountResponse.StoreSummaryDto.class,
        CreateStoreForTenantRequest.class,
        ChangeTenantStatusRequest.class,
        PlatformOverviewResponse.class,
        SaasContractResponse.class,
        SaasInvoiceResponse.class,
        MarkInvoicePaidRequest.class,

        // Shared
        PageResponse.class
})
public class NativeReflectionConfig {
}
