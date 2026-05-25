package ru.gudoshnikova.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Slf4j
@Configuration
public class RestClientConfig {

    @Value("${services.statement.url}")
    private String statementServiceUrl;

    @Value("${services.deal.url}")
    private String dealServiceUrl;

    @Bean
    public RestClient statementRestClient() {
        return createRestClient(statementServiceUrl);
    }

    @Bean
    public RestClient dealRestClient() {
        return createRestClient(dealServiceUrl);
    }

    private RestClient createRestClient(String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
