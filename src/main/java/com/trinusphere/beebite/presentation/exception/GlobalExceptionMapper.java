package com.trinusphere.beebite.presentation.exception;

import com.trinusphere.beebite.domain.shared.exception.DomainException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.stream.Collectors;

/**
 * Mapeador global de exceções para respostas HTTP padronizadas.
 *
 * <p>
 * Graças à hierarquia {@link DomainException}, este mapper resolve TODAS as exceções de domínio via polimorfismo — sem
 * cadeia de {@code instanceof}. Novas exceções de domínio nunca exigem modificação deste mapper (OCP).
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Exception exception) {
        String path = uriInfo != null ? uriInfo.getPath() : "";

        // ── 1. Exceções de Domínio (polimórfico) ──
        if (exception instanceof DomainException ex) {
            // Loga apenas erros 5xx — 4xx são situações esperadas
            if (ex.getHttpStatus() >= 500) {
                LOG.errorf(ex, "DomainException 5xx em %s", path);
            }
            return Response.status(ex.getHttpStatus())
                    .entity(ErrorResponse.of(ex.getHttpStatus(), ex.getErrorType(), ex.getMessage(), path)).build();
        }

        // ── 2. Validação de Bean (Jakarta Validation) ──
        if (exception instanceof ConstraintViolationException ex) {
            String message = ex.getConstraintViolations().stream().map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            return Response.status(400).entity(ErrorResponse.of(400, "Bad Request", message, path)).build();
        }

        // ── 3. IllegalArgumentException (args inválidos) ──
        if (exception instanceof IllegalArgumentException ex) {
            LOG.warnf(ex, "IllegalArgumentException em %s", path);
            return Response.status(400).entity(ErrorResponse.of(400, "Bad Request", ex.getMessage(), path)).build();
        }

        // ── 4. Exceções de segurança (sem acoplamento a io.quarkus.security.*) ──
        // O mapper NÃO importa io.quarkus.security — verifica pelo nome da classe
        // para manter presentation desacoplada de detalhes de framework.
        String exceptionClassName = exception.getClass().getName();
        if (exceptionClassName.equals("io.quarkus.security.ForbiddenException")) {
            return Response.status(403).entity(ErrorResponse.of(403, "Forbidden", "Acesso negado.", path)).build();
        }
        if (exceptionClassName.equals("io.quarkus.security.UnauthorizedException")) {
            return Response.status(401).entity(ErrorResponse.of(401, "Unauthorized", "Autenticação necessária.", path))
                    .build();
        }

        // ── 5. JAX-RS nativas (ForbiddenException, NotFoundException, etc.) ──
        if (exception instanceof WebApplicationException ex) {
            int status = ex.getResponse().getStatus();
            return Response.status(status).entity(
                    ErrorResponse.of(status, ex.getResponse().getStatusInfo().getReasonPhrase(), ex.getMessage(), path))
                    .build();
        }

        // ── 5. Fallback genérico — 500 — SEMPRE loga stack trace completo ──
        LOG.errorf(exception, "Exceção não mapeada em %s [%s]", path, exception.getClass().getName());
        return Response.status(500).entity(
                ErrorResponse.of(500, "Internal Server Error", "Erro interno. Tente novamente mais tarde.", path))
                .build();
    }
}
