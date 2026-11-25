package com.trinusphere.beebite.infrastructure.security;

import com.trinusphere.beebite.application.auth.GetUserUseCase;
import com.trinusphere.beebite.application.auth.LoginUseCase;
import com.trinusphere.beebite.application.auth.dto.LoginRequest;
import com.trinusphere.beebite.application.auth.dto.LoginResponse;
import com.trinusphere.beebite.application.auth.dto.UserResponse;
import com.trinusphere.beebite.application.auth.port.PasswordEncoder;
import com.trinusphere.beebite.domain.auth.entity.User;
import com.trinusphere.beebite.domain.auth.repository.UserRepository;
import com.trinusphere.beebite.domain.auth.valueobject.Role;
import com.trinusphere.beebite.infrastructure.messaging.IfoodOrderSubscriber;
import com.trinusphere.beebite.infrastructure.messaging.PubSubBiteflowEventPublisher;
import com.trinusphere.beebite.infrastructure.messaging.PubSubCatalogEventPublisher;
import com.trinusphere.beebite.presentation.rest.AuthResource;
import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductionSecurityBlockersTest {

    @Mock
    LoginUseCase loginUseCase;

    @Mock
    GetUserUseCase getUserUseCase;

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Bloqueador #1: privateKey.pem e publicKey.pem NÃO devem estar em META-INF/resources (exposição HTTP pública)")
    void keysShouldNotBeInPublicMetaInfResources() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        URL exposedPrivate = cl.getResource("META-INF/resources/privateKey.pem");
        URL exposedPublic = cl.getResource("META-INF/resources/publicKey.pem");

        assertNull(exposedPrivate, "CRÍTICO: privateKey.pem não pode ficar em META-INF/resources/ (servido via HTTP)!");
        assertNull(exposedPublic, "publicKey.pem não deve ficar em META-INF/resources/!");

        URL internalPrivate = cl.getResource("jwt/privateKey.pem");
        URL internalPublic = cl.getResource("jwt/publicKey.pem");

        assertNotNull(internalPrivate, "jwt/privateKey.pem deve existir no classpath interno");
        assertNotNull(internalPublic, "jwt/publicKey.pem deve existir no classpath interno");
    }

    @Test
    @DisplayName("Bloqueador #2: Classes Pub/Sub no beebite-backend devem injetar projectId configurável e respeitar feature flag iFood")
    void pubSubClassesShouldInjectConfigurableProjectIdAndFeatureFlag() throws Exception {
        Field subProjectField = IfoodOrderSubscriber.class.getDeclaredField("projectId");
        ConfigProperty subProjectCfg = subProjectField.getAnnotation(ConfigProperty.class);
        assertNotNull(subProjectCfg, "IfoodOrderSubscriber deve anotar projectId com @ConfigProperty");
        assertEquals("quarkus.google.cloud.project-id", subProjectCfg.name());

        Field subEnabledField = IfoodOrderSubscriber.class.getDeclaredField("ifoodIntegrationEnabled");
        ConfigProperty subEnabledCfg = subEnabledField.getAnnotation(ConfigProperty.class);
        assertNotNull(subEnabledCfg, "IfoodOrderSubscriber deve injetar biteflow.feature.ifood-integration.enabled");
        assertEquals("biteflow.feature.ifood-integration.enabled", subEnabledCfg.name());

        Field bfPubProjectField = PubSubBiteflowEventPublisher.class.getDeclaredField("projectId");
        assertNotNull(bfPubProjectField.getAnnotation(ConfigProperty.class));

        Field catPubProjectField = PubSubCatalogEventPublisher.class.getDeclaredField("projectId");
        assertNotNull(catPubProjectField.getAnnotation(ConfigProperty.class));
    }

    @Test
    @DisplayName("Bloqueador #3: AuthResource deve emitir cookie biteflow_jwt com Secure=true, HttpOnly=true e SameSite configurável em produção")
    void authResourceShouldCreateSecureCookieWithSameSiteInProd() throws Exception {
        AuthResource resource = new AuthResource();

        Field loginField = AuthResource.class.getDeclaredField("loginUseCase");
        loginField.setAccessible(true);
        loginField.set(resource, loginUseCase);

        Field secureField = AuthResource.class.getDeclaredField("cookieSecure");
        secureField.setAccessible(true);
        secureField.set(resource, true);

        Field sameSiteField = AuthResource.class.getDeclaredField("cookieSameSite");
        sameSiteField.setAccessible(true);
        sameSiteField.set(resource, "Lax");

        LoginResponse.UserSummary userSummary = new LoginResponse.UserSummary(
                UUID.randomUUID().toString(), "Admin", "admin", "ADMIN", List.of("dashboard"), UUID.randomUUID().toString()
        );
        when(loginUseCase.execute(any())).thenReturn(new LoginResponse("jwt-token-123", 86400L, userSummary));

        Response response = resource.login(new LoginRequest("admin", "admin123"));
        NewCookie cookie = response.getCookies().get("biteflow_jwt");

        assertNotNull(cookie, "Cookie biteflow_jwt deve ser emitido no login");
        assertTrue(cookie.isSecure(), "Cookie deve ter flag Secure=true quando configurado para produção");
        assertTrue(cookie.isHttpOnly(), "Cookie deve ter flag HttpOnly=true");
        assertEquals(NewCookie.SameSite.LAX, cookie.getSameSite(), "Cookie deve ter SameSite=LAX");
    }

    @Test
    @DisplayName("Bloqueador #4: ProductionAdminSeedGuard deve rotacionar ou desativar admin com senha default admin123 em produção")
    void productionAdminSeedGuardShouldRotateOrDisableDefaultSeedPassword() {
        String defaultSeedHash = "$2a$10$FHNmuZPizma6NEjYV.1.HOWogQSpS0ZbuEJHGpsCodoFhzbvpuEv6";
        User seededAdmin = User.reconstitute(
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "Administrador",
                "admin",
                defaultSeedHash,
                Role.ADMIN,
                List.of(),
                true
        );

        when(userRepository.findByLogin("admin")).thenReturn(Optional.of(seededAdmin));
        when(passwordEncoder.hash("NovaSenhaForte!2026")).thenReturn("$2a$10$newStrongHashValue");

        ProductionAdminSeedGuard guard = new ProductionAdminSeedGuard(
                userRepository,
                passwordEncoder,
                true,
                Optional.of("NovaSenhaForte!2026")
        );

        guard.enforceSeedSecurity();

        assertEquals("$2a$10$newStrongHashValue", seededAdmin.getPasswordHash());
        assertTrue(seededAdmin.isActive());
        verify(userRepository).save(seededAdmin);
    }

    @Test
    @DisplayName("Bloqueador #5: application.properties deve aceitar variáveis BEEBITE_* com fallback seguro para BITEFLOW_*")
    void applicationPropertiesShouldSupportBeebiteEnvVarsWithLegacyBiteflowFallback() throws Exception {
        URL propsUrl = Thread.currentThread().getContextClassLoader().getResource("application.properties");
        assertNotNull(propsUrl, "application.properties deve existir no classpath");

        SmallRyeConfig beebiteConfig = new SmallRyeConfigBuilder()
                .addDefaultInterceptors()
                .withSources(
                        new PropertiesConfigSource(propsUrl),
                        new PropertiesConfigSource(Map.of(
                                "BEEBITE_SYNC_MODE", "EDGE",
                                "BEEBITE_COOKIE_SECURE", "false",
                                "BEEBITE_EDGE_DEVICE_TOKEN", "edge-tok-123",
                                "BEEBITE_CLOUD_SYNC_URL", "https://api.trinusphere.com",
                                "BEEBITE_INITIAL_ADMIN_PASSWORD", "SenhaForte2026!",
                                "BEEBITE_EDGE_STORE_ID", "11111111-2222-3333-4444-555555555555",
                                "BEEBITE_EDGE_STORE_NAME", "Chefs Burguer - Balcão"
                        ), "beebite-env", 500)
                )
                .build();

        assertEquals("EDGE", beebiteConfig.getValue("biteflow.sync.mode", String.class));
        assertFalse(beebiteConfig.getValue("biteflow.auth.cookie.secure", Boolean.class));
        assertEquals("edge-tok-123", beebiteConfig.getValue("biteflow.sync.edge-device-token", String.class));
        assertEquals("https://api.trinusphere.com", beebiteConfig.getValue("biteflow.sync.cloud-url", String.class));
        assertEquals("SenhaForte2026!", beebiteConfig.getValue("biteflow.security.initial-admin-password", String.class));
        assertEquals("11111111-2222-3333-4444-555555555555", beebiteConfig.getValue("biteflow.edge.store-id", String.class));
        assertEquals("Chefs Burguer - Balcão", beebiteConfig.getValue("biteflow.edge.store-name", String.class));

        SmallRyeConfig legacyConfig = new SmallRyeConfigBuilder()
                .addDefaultInterceptors()
                .withSources(
                        new PropertiesConfigSource(propsUrl),
                        new PropertiesConfigSource(Map.of(
                                "BITEFLOW_SYNC_MODE", "EDGE",
                                "BITEFLOW_COOKIE_SECURE", "false",
                                "BITEFLOW_EDGE_DEVICE_TOKEN", "legacy-tok-999"
                        ), "legacy-env", 500)
                )
                .build();

        assertEquals("EDGE", legacyConfig.getValue("biteflow.sync.mode", String.class));
        assertFalse(legacyConfig.getValue("biteflow.auth.cookie.secure", Boolean.class));
        assertEquals("legacy-tok-999", legacyConfig.getValue("biteflow.sync.edge-device-token", String.class));

        SmallRyeConfig defaultConfig = new SmallRyeConfigBuilder()
                .addDefaultInterceptors()
                .withSources(new PropertiesConfigSource(propsUrl))
                .build();

        assertEquals("CLOUD", defaultConfig.getValue("biteflow.sync.mode", String.class));
        assertTrue(defaultConfig.getValue("biteflow.auth.cookie.secure", Boolean.class));
        assertEquals("https://api.trinusphere.com", defaultConfig.getValue("biteflow.sync.cloud-url", String.class));
        assertTrue(defaultConfig.getOptionalValue("biteflow.security.initial-admin-password", String.class).isEmpty());

        // Regressão crítica: todas as propriedades opcionais dos Use Cases de Edge devem ser Optional<String>
        // (ou possuir defaultValue não-vazio) para nunca causar SRCFG00014 no ambiente CLOUD.
        for (Class<?> beanClass : java.util.List.of(
                com.trinusphere.beebite.application.sync.BootstrapEdgeStoreUseCase.class,
                com.trinusphere.beebite.application.sync.BackfillEdgeOutboxUseCase.class,
                com.trinusphere.beebite.application.cash.GetCurrentCashSessionUseCase.class)) {
            for (var ctor : beanClass.getConstructors()) {
                for (var param : ctor.getParameters()) {
                    ConfigProperty cp = param.getAnnotation(ConfigProperty.class);
                    if (cp == null) {
                        continue;
                    }
                    if (param.getType().equals(Optional.class)) {
                        assertDoesNotThrow(() -> defaultConfig.getOptionalValue(cp.name(), String.class),
                                "Optional @ConfigProperty não deve falhar em CLOUD: " + cp.name());
                    } else {
                        String effectiveDefault = cp.defaultValue().equals(ConfigProperty.UNCONFIGURED_VALUE)
                                ? null : cp.defaultValue();
                        if (effectiveDefault == null) {
                            assertDoesNotThrow(() -> defaultConfig.getValue(cp.name(), String.class),
                                    "Propriedade obrigatória @ConfigProperty faltando valor default em application.properties: " + cp.name());
                        }
                    }
                }
            }
        }
    }
}

