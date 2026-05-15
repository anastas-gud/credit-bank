package ru.gudoshnikova.gateway.integration.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

public interface IntegrationService {
    void executePost(RestClient client, String uri, Object body,
                     String operationName, Object... uriVariables);

    void executePostWithQuery(RestClient client, String uri, String queryParam,
                              String queryValue, String operationName, Object... uriVariables);

    <T> T executePostForObject(RestClient client, String uri, Object body,
                           ParameterizedTypeReference<T> responseType,
                           String operationName, Object... uriVariables);

    <T> T executeGet(RestClient client, String uri,
                     ParameterizedTypeReference<T> responseType,
                     String operationName, Object... uriVariables);
}
