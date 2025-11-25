package com.ubik.bookingservice.config;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.List;

/**
 * WebClient filter that propagates user authentication headers from incoming requests
 * to outgoing requests to other microservices.
 */
@Component
public class HeaderPropagationWebClientFilter implements ExchangeFilterFunction {

    private static final List<String> HEADERS_TO_PROPAGATE = List.of(
        "X-User-Id",
        "X-User-Role",
        "X-User-Email"
    );

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        return Mono.deferContextual(ctx -> {
            ClientRequest.Builder requestBuilder = ClientRequest.from(request);

            // Propagate headers from context
            for (String headerName : HEADERS_TO_PROPAGATE) {
                String headerValue = ctx.getOrDefault(headerName, null);
                if (headerValue != null) {
                    requestBuilder.header(headerName, headerValue);
                }
            }

            return next.exchange(requestBuilder.build());
        });
    }
}
