package com.inje.pilly.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient webClient(@Value("${custom.fastapi.base-url}") String fastApiBaseUrl) {
        return WebClient.builder()
                .baseUrl(fastApiBaseUrl)
                .build();
    }
}
