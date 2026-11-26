package com.trinusphere.beebite.application.platform;

import com.trinusphere.beebite.application.auth.port.PasswordEncoder;
import com.trinusphere.beebite.application.platform.dto.OnboardClientRequest;
import com.trinusphere.beebite.application.platform.dto.OnboardClientResponse;
import com.trinusphere.beebite.domain.auth.entity.User;
import com.trinusphere.beebite.domain.auth.entity.UserStoreAccess;
import com.trinusphere.beebite.domain.auth.exception.UserAlreadyExistsException;
import com.trinusphere.beebite.domain.auth.repository.UserRepository;
import com.trinusphere.beebite.domain.auth.repository.UserStoreAccessRepository;
import com.trinusphere.beebite.domain.auth.valueobject.Permission;
import com.trinusphere.beebite.domain.auth.valueobject.Role;
import com.trinusphere.beebite.domain.catalog.entity.Catalog;
import com.trinusphere.beebite.domain.catalog.repository.CatalogRepository;
import com.trinusphere.beebite.domain.platform.entity.SaasContract;
import com.trinusphere.beebite.domain.platform.entity.SaasInvoice;
import com.trinusphere.beebite.domain.platform.entity.TenantAccount;
import com.trinusphere.beebite.domain.platform.repository.SaasContractRepository;
import com.trinusphere.beebite.domain.platform.repository.SaasInvoiceRepository;
import com.trinusphere.beebite.domain.platform.repository.TenantAccountRepository;
import com.trinusphere.beebite.domain.platform.valueobject.BillingCycle;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class OnboardClientTenantUseCase {

    private final TenantAccountRepository tenantAccountRepository;
    private final CatalogRepository catalogRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final UserStoreAccessRepository userStoreAccessRepository;
    private final SaasContractRepository saasContractRepository;
    private final SaasInvoiceRepository saasInvoiceRepository;
    private final PasswordEncoder passwordEncoder;

    public OnboardClientTenantUseCase(
            TenantAccountRepository tenantAccountRepository,
            StoreRepository storeRepository,
            UserRepository userRepository,
            UserStoreAccessRepository userStoreAccessRepository,
            SaasContractRepository saasContractRepository,
            SaasInvoiceRepository saasInvoiceRepository,
            PasswordEncoder passwordEncoder) {
        this(tenantAccountRepository, null, storeRepository, userRepository, userStoreAccessRepository,
                saasContractRepository, saasInvoiceRepository, passwordEncoder);
    }

    @Inject
    public OnboardClientTenantUseCase(
            TenantAccountRepository tenantAccountRepository,
            CatalogRepository catalogRepository,
            StoreRepository storeRepository,
            UserRepository userRepository,
            UserStoreAccessRepository userStoreAccessRepository,
            SaasContractRepository saasContractRepository,
            SaasInvoiceRepository saasInvoiceRepository,
            PasswordEncoder passwordEncoder) {
        this.tenantAccountRepository = tenantAccountRepository;
        this.catalogRepository = catalogRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.userStoreAccessRepository = userStoreAccessRepository;
        this.saasContractRepository = saasContractRepository;
        this.saasInvoiceRepository = saasInvoiceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public OnboardClientResponse execute(OnboardClientRequest request) {
        String cleanLogin = request.ownerLogin().trim().toLowerCase();
        if (userRepository.existsByLogin(cleanLogin)) {
            throw new UserAlreadyExistsException(cleanLogin);
        }

        TenantAccount tenant = new TenantAccount(
                request.companyName(),
                request.tradeName(),
                request.ownerName(),
                request.documentNumber(),
                request.email(),
                request.phone(),
                null
        );
        TenantAccount savedTenant = tenantAccountRepository.save(tenant);

        String defaultCatalogName = "Cardápio Principal - " + savedTenant.getTradeName();
        Catalog defaultCatalog = new Catalog(savedTenant.getId(), defaultCatalogName, true);
        if (catalogRepository != null) {
            defaultCatalog = catalogRepository.save(defaultCatalog);
        } else {
            defaultCatalog.setId(UUID.randomUUID());
        }

        List<Store> createdStores = new ArrayList<>();
        List<OnboardClientResponse.ProvisionedStoreDto> provisionedDtos = new ArrayList<>();

        for (OnboardClientRequest.StoreSetupDto storeSetup : request.stores()) {
            Store store = new Store(storeSetup.name());
            store.updateInfo(storeSetup.name(), null, true, null, storeSetup.address());
            if (storeSetup.publicSlug() != null && !storeSetup.publicSlug().isBlank()) {
                store.definePublicSlug(storeSetup.publicSlug().trim().toLowerCase());
            } else {
                store.definePublicSlug(slugify(storeSetup.name()));
            }
            store.assignToTenant(savedTenant.getId(), storeSetup.cnpj());
            store.assignCatalog(defaultCatalog.getId());

            String mode = storeSetup.syncMode() != null ? storeSetup.syncMode().trim().toUpperCase() : "CLOUD";
            String generatedEdgeToken = null;
            if ("EDGE".equals(mode)) {
                generatedEdgeToken = generateEdgeDeviceToken();
                store.configureEdgeSync("EDGE", generatedEdgeToken);
            } else {
                store.configureEdgeSync("CLOUD", null);
            }

            Store savedStore = storeRepository.save(store);
            createdStores.add(savedStore);

            provisionedDtos.add(new OnboardClientResponse.ProvisionedStoreDto(
                    savedStore.getId().toString(),
                    savedStore.getName(),
                    savedStore.getCnpj(),
                    savedStore.getSyncMode(),
                    generatedEdgeToken,
                    defaultCatalog.getId() != null ? defaultCatalog.getId().toString() : null,
                    defaultCatalog.getName()
            ));
        }

        UUID primaryStoreId = createdStores.get(0).getId();
        List<Permission> ownerPermissions = Arrays.asList(Permission.values());
        String passwordHash = passwordEncoder.hash(request.ownerInitialPassword());

        User ownerUser = new User(
                primaryStoreId,
                request.ownerName(),
                cleanLogin,
                passwordHash,
                Role.ADMIN,
                ownerPermissions
        );
        User savedOwner = userRepository.save(ownerUser);

        for (Store createdStore : createdStores) {
            userStoreAccessRepository.save(new UserStoreAccess(
                    savedOwner.getId(),
                    createdStore.getId(),
                    Role.ADMIN,
                    ownerPermissions
            ));
        }

        String contractIdStr = null;
        if (request.contract() != null) {
            var cReq = request.contract();
            BigDecimal monthlyFee = cReq.monthlyFee() != null ? cReq.monthlyFee() : BigDecimal.ZERO;
            BigDecimal setupFee = cReq.setupFee() != null ? cReq.setupFee() : BigDecimal.ZERO;
            int billingDay = (cReq.billingDay() >= 1 && cReq.billingDay() <= 28) ? cReq.billingDay() : 10;

            SaasContract contract = new SaasContract(
                    savedTenant.getId(),
                    cReq.planName(),
                    BillingCycle.MONTHLY,
                    monthlyFee,
                    setupFee,
                    Math.max(1, createdStores.size()),
                    cReq.includesIfood(),
                    cReq.includesEdgeSync(),
                    billingDay,
                    LocalDate.now(),
                    "Contrato inicial gerado no Onboarding"
            );
            SaasContract savedContract = saasContractRepository.save(contract);
            contractIdStr = savedContract.getId() != null ? savedContract.getId().toString() : null;

            if (setupFee.compareTo(BigDecimal.ZERO) > 0 && savedContract.getId() != null) {
                SaasInvoice setupInvoice = new SaasInvoice(
                        savedContract.getId(),
                        savedTenant.getId(),
                        "SETUP",
                        "Taxa de Implantação / Setup — " + savedTenant.getTradeName(),
                        setupFee,
                        LocalDate.now().plusDays(3)
                );
                saasInvoiceRepository.save(setupInvoice);
            }

            if (monthlyFee.compareTo(BigDecimal.ZERO) > 0 && savedContract.getId() != null) {
                YearMonth currentYm = YearMonth.now();
                int day = Math.min(billingDay, currentYm.lengthOfMonth());
                LocalDate dueDate = currentYm.atDay(day);
                if (dueDate.isBefore(LocalDate.now())) {
                    dueDate = currentYm.plusMonths(1).atDay(billingDay);
                }
                String refMonth = YearMonth.from(dueDate).toString();
                SaasInvoice firstMonthly = new SaasInvoice(
                        savedContract.getId(),
                        savedTenant.getId(),
                        refMonth,
                        String.format("Mensalidade %s (%d loja(s)) — %s", cReq.planName(), createdStores.size(), refMonth),
                        monthlyFee,
                        dueDate
                );
                saasInvoiceRepository.save(firstMonthly);
            }
        }

        return new OnboardClientResponse(
                savedTenant.getId().toString(),
                savedTenant.getTradeName(),
                savedOwner.getId() != null ? savedOwner.getId().toString() : null,
                savedOwner.getLogin(),
                contractIdStr,
                provisionedDtos
        );
    }

    static String generateEdgeDeviceToken() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return "bf_edge_" + HexFormat.of().formatHex(bytes);
    }

    static String slugify(String text) {
        if (text == null || text.isBlank()) {
            return "loja-" + UUID.randomUUID().toString().substring(0, 6);
        }
        return text.trim().toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
