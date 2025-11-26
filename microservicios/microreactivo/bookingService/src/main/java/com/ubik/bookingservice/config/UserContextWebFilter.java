package com.ubik.bookingservice.config;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.List;

/**
 * Web filter that extracts user authentication headers from incoming requests
 * and stores them in the Reactor context for propagation to downstream services.
 */
@Component
public class UserContextWebFilter implements WebFilter {

    private static final List<String> HEADERS_TO_EXTRACT = List.of(
        "X-User-Id",
        "X-User-Role",
        "X-User-Email"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange)
            .contextWrite(ctx -> {
                Context context = ctx;
                for (String headerName : HEADERS_TO_EXTRACT) {
                    List<String> headerValues = exchange.getRequest().getHeaders().get(headerName);
                    if (headerValues != null && !headerValues.isEmpty()) {
                        context = context.put(headerName, headerValues.get(0));
                    }
                }
                return context;
            });
    }
}
