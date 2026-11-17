package com.trinusphere.beebite.presentation.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuperAdminReadOnlyStoreFilterTest {

    private SuperAdminReadOnlyStoreFilter filter;

    @Mock
    ContainerRequestContext requestContext;
    @Mock
    SecurityContext securityContext;
    @Mock
    UriInfo uriInfo;

    @BeforeEach
    void setUp() {
        filter = new SuperAdminReadOnlyStoreFilter();
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

        filter.filter(requestContext);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext, times(1)).abortWith(captor.capture());
        assertEquals(403, captor.getValue().getStatus());
    }

    @Test
    void shouldBlockPutAndDeleteForSuperAdminOnStoreProductsOrStoreSettings() {
        when(securityContext.isUserInRole("SUPER_ADMIN")).thenReturn(true);

        when(requestContext.getMethod()).thenReturn("PUT");
        when(uriInfo.getPath()).thenReturn("/api/v1/store");
        filter.filter(requestContext);

        when(requestContext.getMethod()).thenReturn("DELETE");
        when(uriInfo.getPath()).thenReturn("/api/v1/products/123");
        filter.filter(requestContext);

        verify(requestContext, times(2)).abortWith(any(Response.class));
    }

    @Test
    void shouldAllowWriteRequestsForRegularStoreAdmin() {
        when(securityContext.isUserInRole("SUPER_ADMIN")).thenReturn(false);

        filter.filter(requestContext);

        verify(requestContext, never()).abortWith(any());
    }
}
