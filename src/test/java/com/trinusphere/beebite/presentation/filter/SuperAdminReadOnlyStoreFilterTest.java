package com.trinusphere.beebite.presentation.filter;

import com.trinusphere.beebite.application.platform.CanSuperAdminMutateStoreUseCase;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuperAdminReadOnlyStoreFilterTest {

    private SuperAdminReadOnlyStoreFilter filter;

    @Mock
    CanSuperAdminMutateStoreUseCase canSuperAdminMutateStoreUseCase;
    @Mock
    JsonWebToken jwt;
    @Mock
    ContainerRequestContext requestContext;
    @Mock
    SecurityContext securityContext;
    @Mock
    UriInfo uriInfo;

    private static final UUID CLIENT_STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID BETA_STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() {
        filter = new SuperAdminReadOnlyStoreFilter(canSuperAdminMutateStoreUseCase, jwt);
        lenient().when(requestContext.getSecurityContext()).thenReturn(securityContext);
        lenient().when(requestContext.getUriInfo()).thenReturn(uriInfo);
    }

    @Test
    void shouldAllowGetRequestsForSuperAdminOnStoreRoutes() {
        when(securityContext.isUserInRole("SUPER_ADMIN")).thenReturn(true);
        when(requestContext.getMethod()).thenReturn("GET");

        filter.filter(requestContext);

        verify(requestContext, never()).abortWith(any());
    }

    @Test
    void shouldAllowWriteRequestsForSuperAdminOnPlatformAndAuthRoutes() {
        when(securityContext.isUserInRole("SUPER_ADMIN")).thenReturn(true);
        when(requestContext.getMethod()).thenReturn("POST");

        when(uriInfo.getPath()).thenReturn("/api/v1/platform/tenants/onboard");
        filter.filter(requestContext);

        when(uriInfo.getPath()).thenReturn("/api/v1/auth/switch-store/123");
        filter.filter(requestContext);

        verify(requestContext, never()).abortWith(any());
    }

    @Test
    void shouldBlockWriteRequestsForSuperAdminInsideClientStoreEndpoints() {
        when(securityContext.isUserInRole("SUPER_ADMIN")).thenReturn(true);
        when(requestContext.getMethod()).thenReturn("POST");
        when(uriInfo.getPath()).thenReturn("/api/v1/orders");
        when(jwt.getClaim("storeId")).thenReturn(CLIENT_STORE_ID.toString());
        when(canSuperAdminMutateStoreUseCase.execute(CLIENT_STORE_ID)).thenReturn(false);

        filter.filter(requestContext);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext, times(1)).abortWith(captor.capture());
        assertEquals(403, captor.getValue().getStatus());
    }

    @Test
    void shouldAllowWriteRequestsForSuperAdminInsideTrinusphereBetaStore() {
        when(securityContext.isUserInRole("SUPER_ADMIN")).thenReturn(true);
        when(requestContext.getMethod()).thenReturn("POST");
        when(uriInfo.getPath()).thenReturn("/api/v1/products");
        when(jwt.getClaim("storeId")).thenReturn(BETA_STORE_ID.toString());
        when(canSuperAdminMutateStoreUseCase.execute(BETA_STORE_ID)).thenReturn(true);

        filter.filter(requestContext);

        verify(requestContext, never()).abortWith(any());
    }

    @Test
    void shouldAllowWriteRequestsForRegularStoreAdmin() {
        when(securityContext.isUserInRole("SUPER_ADMIN")).thenReturn(false);

        filter.filter(requestContext);

        verify(requestContext, never()).abortWith(any());
    }
}
