package ru.gudoshnikova.deal.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import ru.gudoshnikova.deal.exception.CalculatorServiceException;
import ru.gudoshnikova.deal.api.dto.ErrorResponseDto;

@Slf4j
@Configuration
public class RestClientConfig {
    private final ObjectMapper objectMapper;

    @Value("${calculator.service.url}")
    private String calculatorServiceUrl;

    @Autowired
    public RestClientConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public RestClient calculatorRestClient() {
        return RestClient.builder()
                .baseUrl(calculatorServiceUrl)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (request, response) -> {
                    String responseBody = new String(response.getBody().readAllBytes());
                    log.error("Calculator service returned error: {}", responseBody);

                    ErrorResponseDto errorResponse = objectMapper.readValue(
                            responseBody,
                            ErrorResponseDto.class
                    );

                    String errorMessage = errorResponse.getMessage();
                    String errorType = errorResponse.getError();

                    log.error("Calculator error: type={}, message={}", errorType, errorMessage);

                    throw new CalculatorServiceException(
                            String.format("%s: %s", errorType, errorMessage)
                    );
                })
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (request, response) -> {
                    log.error("Calculator service internal server error");
                    throw new CalculatorServiceException("Calculator service internal server error");
                })
                .build();
    }
}
