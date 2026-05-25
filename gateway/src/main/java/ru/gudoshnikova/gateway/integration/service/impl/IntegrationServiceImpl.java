package ru.gudoshnikova.gateway.integration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ru.gudoshnikova.gateway.exception.ClientHttpException;
import ru.gudoshnikova.gateway.exception.ExternalServiceException;
import ru.gudoshnikova.gateway.integration.service.IntegrationService;


@Slf4j
@Service
public class IntegrationServiceImpl implements IntegrationService {

    private static final String ERROR_REST_CLIENT = "Gateway: RestClientException while executing '{}', operation: {}";
    private static final String ERROR_SERVER_ERROR = "Gateway: Server error while executing '{}', status code: {}";
    private static final String ERROR_CLIENT_ERROR = "Gateway: Client error while executing '{}', status code: {}";

    private static final String EXTERNAL_SERVICE_ERROR = "Failed to execute operation: {}";
    private static final String INTERNAL_SERVER_ERROR = "Deal or statement service internal error";
    private static final String CLIENT_ERROR_TEMPLATE = "Client error with status code: {}";

    @Override
    public void executePost(RestClient client, String uri, Object body, String operationName, Object... uriVariables) {
        try {
            RestClient.RequestBodySpec request = client.post().uri(uri, uriVariables);
            if (body != null) {
                request.body(body);
            }
            request.retrieve()
                    .onStatus(HttpStatusCode::is5xxServerError, getServerErrorHandler(operationName))
                    .onStatus(HttpStatusCode::is4xxClientError, getClientErrorHandler(operationName))
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.error(ERROR_REST_CLIENT, operationName, e.getMessage(), e);
            throw new ExternalServiceException(String.format(EXTERNAL_SERVICE_ERROR, operationName), e);
        }
    }

    @Override
    public void executePostWithQuery(RestClient client, String uri, String queryParam, String queryValue, String operationName, Object... uriVariables) {
        try {
            client.post()
                    .uri(uriBuilder -> uriBuilder
                            .path(uri)
                            .queryParam(queryParam, queryValue)
                            .build(uriVariables))
                    .retrieve()
                    .onStatus(HttpStatusCode::is5xxServerError, getServerErrorHandler(operationName))
                    .onStatus(HttpStatusCode::is4xxClientError, getClientErrorHandler(operationName))
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.error(ERROR_REST_CLIENT, operationName, e.getMessage(), e);
            throw new ExternalServiceException(String.format(EXTERNAL_SERVICE_ERROR, operationName), e);
        }
    }

    @Override
    public <T> T executePostForObject(RestClient client, String uri, Object body, ParameterizedTypeReference<T> responseType, String operationName, Object... uriVariables) {
        try {
            RestClient.RequestBodySpec request = client.post().uri(uri, uriVariables);
            if (body != null) {
                request.body(body);
            }
            return request
                    .retrieve()
                    .onStatus(HttpStatusCode::is5xxServerError, getServerErrorHandler(operationName))
                    .onStatus(HttpStatusCode::is4xxClientError, getClientErrorHandler(operationName))
                    .body(responseType);
        } catch (RestClientException e) {
            log.error(ERROR_REST_CLIENT, operationName, e.getMessage(), e);
            throw new ExternalServiceException(String.format(EXTERNAL_SERVICE_ERROR, operationName), e);
        }
    }

    @Override
    public <T> T executeGet(RestClient client, String uri, ParameterizedTypeReference<T> responseType, String operationName, Object... uriVariables) {
        try {
            return client.get()
                    .uri(uri, uriVariables)
                    .retrieve()
                    .onStatus(HttpStatusCode::is5xxServerError, getServerErrorHandler(operationName))
                    .onStatus(HttpStatusCode::is4xxClientError, getClientErrorHandler(operationName))
                    .body(responseType);
        } catch (RestClientException e) {
            log.error(ERROR_REST_CLIENT, operationName, e.getMessage(), e);
            throw new ExternalServiceException(String.format(EXTERNAL_SERVICE_ERROR, operationName), e);
        }
    }

    private RestClient.ResponseSpec.ErrorHandler getServerErrorHandler(String operationName) {
        return (request, response) -> {
            log.error(ERROR_SERVER_ERROR, operationName, response.getStatusCode());
            throw new ExternalServiceException(INTERNAL_SERVER_ERROR);
        };
    }

    private RestClient.ResponseSpec.ErrorHandler getClientErrorHandler(String operationName) {
        return (request, response) -> {
            log.error(ERROR_CLIENT_ERROR, operationName, response.getStatusCode());
            throw new ClientHttpException(String.format(CLIENT_ERROR_TEMPLATE, response.getStatusCode()));
        };
    }
}
