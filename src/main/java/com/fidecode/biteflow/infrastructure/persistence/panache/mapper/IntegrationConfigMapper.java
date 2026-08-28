package com.fidecode.biteflow.infrastructure.persistence.panache.mapper;
import com.fidecode.biteflow.domain.integration.entity.IntegrationConfig;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.IntegrationConfigEntity;
public final class IntegrationConfigMapper { private IntegrationConfigMapper() {}
 public static IntegrationConfig toDomain(IntegrationConfigEntity e){return IntegrationConfig.reconstitute(e.id,e.storeId,e.provider,e.status,e.externalStoreId,e.clientId,e.clientSecret,e.accessToken,e.businessAccountId,e.phoneNumber,e.apiProvider,e.storeStatus);}
 public static IntegrationConfigEntity toEntity(IntegrationConfig c){var e=new IntegrationConfigEntity();e.id=c.getId();e.storeId=c.getStoreId();e.provider=c.getProvider();e.status=c.getStatus();e.externalStoreId=c.getExternalStoreId();e.clientId=c.getClientId();e.clientSecret=c.getClientSecret();e.accessToken=c.getAccessToken();e.businessAccountId=c.getBusinessAccountId();e.phoneNumber=c.getPhoneNumber();e.apiProvider=c.getApiProvider();e.storeStatus=c.getStoreStatus();return e;}
}
