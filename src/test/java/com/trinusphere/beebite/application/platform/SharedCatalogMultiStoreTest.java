package com.trinusphere.beebite.application.platform;

import com.trinusphere.beebite.application.auth.port.PasswordEncoder;
import com.trinusphere.beebite.application.catalog.dto.ProductAvailabilityRequest;
import com.trinusphere.beebite.application.catalog.dto.ProductResponse;
import com.trinusphere.beebite.application.catalog.port.CatalogEventPublisher;
import com.trinusphere.beebite.application.catalog.product.ProductResponseAssembler;
import com.trinusphere.beebite.application.catalog.product.UpdateProductAvailabilityUseCase;
import com.trinusphere.beebite.application.platform.dto.CatalogSummaryDto;
import com.trinusphere.beebite.application.platform.dto.CreateStoreForTenantRequest;
import com.trinusphere.beebite.application.platform.dto.OnboardClientRequest;
import com.trinusphere.beebite.application.platform.dto.OnboardClientResponse;
import com.trinusphere.beebite.domain.auth.entity.User;
import com.trinusphere.beebite.domain.auth.entity.UserStoreAccess;
import com.trinusphere.beebite.domain.auth.repository.UserRepository;
import com.trinusphere.beebite.domain.auth.repository.UserStoreAccessRepository;
import com.trinusphere.beebite.domain.catalog.entity.Catalog;
import com.trinusphere.beebite.domain.catalog.entity.Product;
import com.trinusphere.beebite.domain.catalog.repository.CatalogRepository;
import com.trinusphere.beebite.domain.catalog.repository.IngredientRepository;
import com.trinusphere.beebite.domain.catalog.repository.ProductCategoryRepository;
import com.trinusphere.beebite.domain.catalog.repository.ProductRepository;
import com.trinusphere.beebite.domain.catalog.valueobject.ChannelPrice;
import com.trinusphere.beebite.domain.catalog.valueobject.ProductStatus;
import com.trinusphere.beebite.domain.platform.entity.TenantAccount;
import com.trinusphere.beebite.domain.platform.repository.SaasContractRepository;
import com.trinusphere.beebite.domain.platform.repository.SaasInvoiceRepository;
import com.trinusphere.beebite.domain.platform.repository.TenantAccountRepository;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SharedCatalogMultiStoreTest {

    @Mock
    TenantAccountRepository tenantAccountRepository;
    @Mock
    CatalogRepository catalogRepository;
    @Mock
    StoreRepository storeRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    UserStoreAccessRepository userStoreAccessRepository;
    @Mock
    SaasContractRepository saasContractRepository;
    @Mock
    SaasInvoiceRepository saasInvoiceRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    ProductRepository productRepository;
    @Mock
    IngredientRepository ingredientRepository;
    @Mock
    ProductCategoryRepository categoryRepository;
    @Mock
    CatalogEventPublisher catalogEventPublisher;

    private OnboardClientTenantUseCase onboardClientUseCase;
    private AddStoreToTenantUseCase addStoreToTenantUseCase;
    private ListTenantCatalogsUseCase listTenantCatalogsUseCase;
    private UpdateProductAvailabilityUseCase updateProductAvailabilityUseCase;

    @BeforeEach
    void setUp() {
        onboardClientUseCase = new OnboardClientTenantUseCase(
                tenantAccountRepository,
                catalogRepository,
                storeRepository,
                userRepository,
                userStoreAccessRepository,
                saasContractRepository,
                saasInvoiceRepository,
                passwordEncoder
        );
        addStoreToTenantUseCase = new AddStoreToTenantUseCase(
                tenantAccountRepository,
                catalogRepository,
                storeRepository,
                userRepository,
                userStoreAccessRepository
        );
        listTenantCatalogsUseCase = new ListTenantCatalogsUseCase(
                tenantAccountRepository,
                catalogRepository,
                storeRepository
        );
        updateProductAvailabilityUseCase = new UpdateProductAvailabilityUseCase(
                productRepository,
                new ProductResponseAssembler(ingredientRepository, categoryRepository),
                catalogEventPublisher
        );
    }

    @Test
    void shouldOnboardEdgeStoreFirstAndAutomaticallyShareSameCatalogWhenAddingCloudDeliveryLater() {
        UUID generatedTenantId = UUID.randomUUID();
        UUID generatedCatalogId = UUID.randomUUID();

        when(userRepository.existsByLogin("chefsburguer")).thenReturn(false);
        when(passwordEncoder.hash(any())).thenReturn("hash");
        when(tenantAccountRepository.save(any(TenantAccount.class))).thenAnswer(inv -> {
            TenantAccount t = inv.getArgument(0);
            t.setId(generatedTenantId);
            return t;
        });
        when(catalogRepository.save(any(Catalog.class))).thenAnswer(inv -> {
            Catalog c = inv.getArgument(0);
            if (c.getId() == null) {
                c.setId(generatedCatalogId);
            }
            return c;
        });
        when(storeRepository.save(any(Store.class))).thenAnswer(inv -> {
            Store s = inv.getArgument(0);
            if (s.getId() == null) {
                s.setId(UUID.randomUUID());
            }
            return s;
        });
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(userStoreAccessRepository.save(any(UserStoreAccess.class))).thenAnswer(inv -> inv.getArgument(0));

        // Mês 1: Cliente implanta APENAS a unidade Balcão (EDGE)
        OnboardClientRequest onboardReq = new OnboardClientRequest(
                "Chefs Burguer Ltda",
                "Chefs Burguer",
                "João",
                "45.123.001/0001-10",
                "joao@chefs.com",
                "11999999999",
                "chefsburguer",
                "Senha@123",
                List.of(new OnboardClientRequest.StoreSetupDto(
                        "Chefs Burguer - Balcão", "45.123.001/0001-10", "Av. Central, 100", "chefs-balcao", "EDGE")),
                null
        );

        OnboardClientResponse onboardRes = onboardClientUseCase.execute(onboardReq);
        assertEquals(1, onboardRes.stores().size());
        var edgeStoreDto = onboardRes.stores().get(0);
        assertEquals("EDGE", edgeStoreDto.syncMode());
        assertEquals(generatedCatalogId.toString(), edgeStoreDto.catalogId());
        assertEquals("Cardápio Principal - Chefs Burguer", edgeStoreDto.catalogName());

        // Mês 3: Cliente adiciona a unidade Delivery (CLOUD) sem informar catalogId -> herda o mesmo Cardápio!
        TenantAccount existingTenant = new TenantAccount("Chefs Burguer Ltda", "Chefs Burguer", "João", null, null, null, null);
        existingTenant.setId(generatedTenantId);
        Catalog defaultCatalog = Catalog.reconstitute(generatedCatalogId, generatedTenantId, "Cardápio Principal - Chefs Burguer", true, null, null);

        when(tenantAccountRepository.findById(generatedTenantId)).thenReturn(Optional.of(existingTenant));
        when(catalogRepository.findDefaultByTenantId(generatedTenantId)).thenReturn(Optional.of(defaultCatalog));

        OnboardClientResponse.ProvisionedStoreDto deliveryStoreDto = addStoreToTenantUseCase.execute(
                generatedTenantId,
                new CreateStoreForTenantRequest(
                        "Chefs Burguer - Delivery",
                        "45.123.002/0001-99",
                        "Rua Delivery, 50",
                        "chefs-delivery",
                        "CLOUD",
                        "chefsburguer",
                        null,
                        null
                )
        );

        assertEquals("CLOUD", deliveryStoreDto.syncMode());
        assertEquals(generatedCatalogId.toString(), deliveryStoreDto.catalogId());
        assertEquals("Cardápio Principal - Chefs Burguer", deliveryStoreDto.catalogName());
    }

    @Test
    void shouldCreateSeparateCatalogWhenTenantAddsStoreForSecondBrand() {
        UUID tenantId = UUID.randomUUID();
        UUID secondCatalogId = UUID.randomUUID();
        TenantAccount existingTenant = new TenantAccount("Grupo Chefs Ltda", "Grupo Chefs", "João", null, null, null, null);
        existingTenant.setId(tenantId);

        when(tenantAccountRepository.findById(tenantId)).thenReturn(Optional.of(existingTenant));
        when(catalogRepository.save(any(Catalog.class))).thenAnswer(inv -> {
            Catalog c = inv.getArgument(0);
            c.setId(secondCatalogId);
            return c;
        });
        when(storeRepository.save(any(Store.class))).thenAnswer(inv -> {
            Store s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        var created = addStoreToTenantUseCase.execute(
                tenantId,
                new CreateStoreForTenantRequest(
                        "Chefs Açaí",
                        null,
                        "Av. Praia, 10",
                        "chefs-acai",
                        "CLOUD",
                        null,
                        null,
                        "Cardápio Açaí & Sorvetes"
                )
        );

        assertEquals(secondCatalogId.toString(), created.catalogId());
        assertEquals("Cardápio Açaí & Sorvetes", created.catalogName());

        when(catalogRepository.findAllByTenantId(tenantId)).thenReturn(List.of(
                Catalog.reconstitute(secondCatalogId, tenantId, "Cardápio Açaí & Sorvetes", false, null, null)
        ));
        when(storeRepository.findAll()).thenReturn(List.of());
        List<CatalogSummaryDto> summaries = listTenantCatalogsUseCase.execute(tenantId);
        assertEquals(1, summaries.size());
        assertEquals("Cardápio Açaí & Sorvetes", summaries.get(0).name());
    }

    @Test
    void shouldDistinguishStoreOnlyPauseFromGlobalCatalogPauseWithIfoodChannelPrice() {
        UUID catalogId = UUID.randomUUID();
        UUID balcaoStoreId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Product product = Product.forCatalog(catalogId, balcaoStoreId, "X-Bacon Artesanal", null, new BigDecimal("28.00"));
        product.setId(productId);
        product.replaceChannelPrices(List.of(new ChannelPrice("IFOOD", new BigDecimal("34.90"))));

        when(productRepository.findByIdInStore(productId, balcaoStoreId)).thenReturn(Optional.of(product));

        // 1. Pausa APENAS na loja atual (STORE_ONLY) -> NÃO publica no iFood!
        ProductResponse pausedLocal = updateProductAvailabilityUseCase.execute(
                balcaoStoreId,
                productId,
                new ProductAvailabilityRequest("UNAVAILABLE", true, true, "STORE_ONLY")
        );

        assertEquals("UNAVAILABLE", pausedLocal.status());
        assertEquals("AVAILABLE", pausedLocal.globalStatus());
        assertEquals("UNAVAILABLE", pausedLocal.localStatus());
        verify(productRepository).updateStoreLocalStatus(balcaoStoreId, productId, ProductStatus.UNAVAILABLE);
        verifyNoInteractions(catalogEventPublisher);

        // 2. Pausa GLOBAL no cardápio inteiro (CATALOG_GLOBAL) -> Publica no Pub/Sub com o preço do canal IFOOD (34.90)!
        when(productRepository.saveForStore(any(Product.class), eq(balcaoStoreId))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse pausedGlobal = updateProductAvailabilityUseCase.execute(
                balcaoStoreId,
                productId,
                new ProductAvailabilityRequest("UNAVAILABLE", true, true, "CATALOG_GLOBAL")
        );

        assertEquals("UNAVAILABLE", pausedGlobal.globalStatus());
        verify(catalogEventPublisher).publishCatalogChanged(
                catalogId,
                balcaoStoreId,
                productId,
                "PRODUCT_UPDATED",
                new BigDecimal("34.90"),
                "UNAVAILABLE"
        );
    }
}

