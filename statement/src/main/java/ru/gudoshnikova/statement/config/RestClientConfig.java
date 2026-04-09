package ru.gudoshnikova.statement.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import ru.gudoshnikova.statement.dto.ErrorResponseDto;
import ru.gudoshnikova.statement.exception.DealServiceException;

@Slf4j
@Configuration
public class RestClientConfig {

    private final ObjectMapper objectMapper;

    @Value("${deal.service.url}")
    private String dealServiceUrl;

    @Autowired
    public RestClientConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public RestClient dealRestClient() {
        return RestClient.builder()
                .baseUrl(dealServiceUrl)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (request, response) -> {
                    String responseBody = new String(response.getBody().readAllBytes());
                    log.error("Deal service returned error: {}", responseBody);

                    ErrorResponseDto errorResponse = objectMapper.readValue(
                            responseBody,
                            ErrorResponseDto.class
                    );

                    String errorMessage = errorResponse.getMessage();
                    String errorType = errorResponse.getError();

                    log.error("Deal error: type={}, message={}", errorType, errorMessage);

                    throw new DealServiceException(
                            String.format("%s: %s", errorType, errorMessage)
                    );
                })
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (request, response) -> {
                    log.error("Deal service internal server error");
                    throw new DealServiceException("Deal service internal server error");
                })
                .build();
    }
}
