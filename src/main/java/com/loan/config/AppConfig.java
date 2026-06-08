package com.loan.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    public static final String TRACE_ID = "traceId";

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder()
                .filter(traceIdPropagationFilter());
    }

    private ExchangeFilterFunction traceIdPropagationFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest ->
            clientRequest.attribute(TRACE_ID)
                .<reactor.core.publisher.Mono<ClientRequest>>map(id ->
                    reactor.core.publisher.Mono.just(
                        ClientRequest.from(clientRequest)
                            .header("X-Trace-Id", (String) id)
                            .build()
                    )
                )
                .orElseGet(() ->
                    reactor.core.publisher.Mono.deferContextual(ctx -> {
                        String traceId = ctx.getOrDefault(TRACE_ID, null);
                        if (traceId != null) {
                            return reactor.core.publisher.Mono.just(
                                ClientRequest.from(clientRequest)
                                    .header("X-Trace-Id", traceId)
                                    .build()
                            );
                        }
                        return reactor.core.publisher.Mono.just(clientRequest);
                    })
                )
        );
    }
}
