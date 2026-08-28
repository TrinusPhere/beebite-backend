package com.fidecode.biteflow.domain.integration.entity;

import com.fidecode.biteflow.domain.shared.StoreGuard;
import java.util.UUID;

public class IntegrationConfig implements StoreGuard.StoreScoped {
    private UUID id;
    private final UUID storeId;
    private final String provider;
    private String status;
    private String externalStoreId;
    private String clientId;
    private String clientSecret;
    private String accessToken;
    private String businessAccountId;
    private String phoneNumber;
    private String apiProvider;
    private String storeStatus;

    public IntegrationConfig(UUID storeId, String provider) {
        if (storeId == null) throw new IllegalArgumentException("storeId é obrigatório.");
        if (provider == null || provider.isBlank()) throw new IllegalArgumentException("provider é obrigatório.");
        this.storeId = storeId;
        this.provider = provider.toUpperCase();
        this.status = "disconnected";
    }

    public static IntegrationConfig reconstitute(UUID id, UUID storeId, String provider, String status,
            String externalStoreId, String clientId, String clientSecret, String accessToken,
            String businessAccountId, String phoneNumber, String apiProvider, String storeStatus) {
        var c = new IntegrationConfig(storeId, provider);
        c.id = id; c.status = status; c.externalStoreId = externalStoreId; c.clientId = clientId;
        c.clientSecret = clientSecret; c.accessToken = accessToken; c.businessAccountId = businessAccountId;
        c.phoneNumber = phoneNumber; c.apiProvider = apiProvider; c.storeStatus = storeStatus;
        return c;
    }

    public void configureWhatsapp(String apiProvider, String phoneNumber, String businessAccountId, String newAccessToken) {
        this.apiProvider = apiProvider;
        this.phoneNumber = phoneNumber;
        this.businessAccountId = businessAccountId;
        if (newAccessToken != null && !newAccessToken.isBlank()) this.accessToken = newAccessToken;
        this.status = hasText(phoneNumber) && hasText(businessAccountId) ? "configured" : "disconnected";
    }

    public void configureIfood(String clientId, String newClientSecret, String merchantId) {
        this.clientId = clientId;
        if (newClientSecret != null && !newClientSecret.isBlank()) this.clientSecret = newClientSecret;
        this.externalStoreId = merchantId;
        this.status = "configured";
        if (this.storeStatus == null) this.storeStatus = "closed";
    }

    public void markConfigured() { this.status = "configured"; }
    public void changeStoreAvailability(boolean open) { this.storeStatus = open ? "open" : "closed"; }
    private static boolean hasText(String value) { return value != null && !value.isBlank(); }

    public UUID getId(){return id;} public UUID getStoreId(){return storeId;} public String getProvider(){return provider;}
    public String getStatus(){return status;} public String getExternalStoreId(){return externalStoreId;}
    public String getClientId(){return clientId;} public String getClientSecret(){return clientSecret;}
    public String getAccessToken(){return accessToken;} public String getBusinessAccountId(){return businessAccountId;}
    public String getPhoneNumber(){return phoneNumber;} public String getApiProvider(){return apiProvider;}
    public String getStoreStatus(){return storeStatus;} public void setId(UUID id){this.id=id;}
}
