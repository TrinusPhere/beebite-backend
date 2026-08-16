package com.fidecode.biteflow.presentation.filter;

import io.vertx.ext.web.Router;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

/**
 * Custom CORS filter to ensure PATCH method is included in Access-Control-Allow-Methods.
 * Replaces the default Quarkus CORS handler which doesn't include PATCH properly.
 */
@ApplicationScoped
public class CorsFilter {

    private static final String ALLOW_METHODS = "GET,POST,PUT,PATCH,DELETE,OPTIONS,HEAD";
    private static final String ALLOW_HEADERS = "accept,authorization,content-type,x-requested-with,origin";

    public void init(@Observes Router router) {
        router.route().order(-100).handler(ctx -> {
            String origin = ctx.request().getHeader("Origin");

            if (origin != null) {
                // Use headersEndHandler to guarantee our headers are set LAST and deduplicated
                ctx.addHeadersEndHandler(v -> {
                    var headers = ctx.response().headers();
                    // Remove ALL CORS headers first
                    headers.remove("Access-Control-Allow-Origin");
                    headers.remove("Access-Control-Allow-Methods");
                    headers.remove("Access-Control-Allow-Headers");
                    headers.remove("Access-Control-Allow-Credentials");
                    headers.remove("Access-Control-Max-Age");
                    headers.remove("Access-Control-Expose-Headers");
                    // Set our own
                    headers.set("Access-Control-Allow-Origin", origin);
                    headers.set("Access-Control-Allow-Methods", ALLOW_METHODS);
                    headers.set("Access-Control-Allow-Headers", ALLOW_HEADERS);
                    headers.set("Access-Control-Allow-Credentials", "true");
                    headers.set("Access-Control-Max-Age", "86400");
                });
            }

            if ("OPTIONS".equalsIgnoreCase(ctx.request().method().name())) {
                if (origin != null) {
                    ctx.response()
                        .putHeader("Access-Control-Allow-Origin", origin)
                        .putHeader("Access-Control-Allow-Methods", ALLOW_METHODS)
                        .putHeader("Access-Control-Allow-Headers", ALLOW_HEADERS)
                        .putHeader("Access-Control-Allow-Credentials", "true")
                        .putHeader("Access-Control-Max-Age", "86400");
                }
                ctx.response().setStatusCode(204).end();
                return;
            }

            ctx.next();
        });
    }
}
