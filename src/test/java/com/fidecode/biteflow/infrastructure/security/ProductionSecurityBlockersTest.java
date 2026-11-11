package com.fidecode.biteflow.infrastructure.security;

import com.fidecode.biteflow.application.auth.GetUserUseCase;
import com.fidecode.biteflow.application.auth.LoginUseCase;
import com.fidecode.biteflow.application.auth.dto.LoginRequest;
import com.fidecode.biteflow.application.auth.dto.LoginResponse;
import com.fidecode.biteflow.application.auth.dto.UserResponse;
import com.fidecode.biteflow.application.auth.port.PasswordEncoder;
import com.fidecode.biteflow.domain.auth.entity.User;
import com.fidecode.biteflow.domain.auth.repository.UserRepository;
import com.fidecode.biteflow.domain.auth.valueobject.Role;
import com.fidecode.biteflow.infrastructure.messaging.IfoodOrderSubscriber;
import com.fidecode.biteflow.infrastructure.messaging.PubSubBiteflowEventPublisher;
import com.fidecode.biteflow.infrastructure.messaging.PubSubCatalogEventPublisher;
import com.fidecode.biteflow.presentation.rest.AuthResource;
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
    @DisplayName("Bloqueador #2: Classes Pub/Sub no bite-flow devem injetar projectId configurável e respeitar feature flag iFood")
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
}

