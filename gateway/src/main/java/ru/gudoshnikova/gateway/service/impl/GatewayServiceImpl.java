package ru.gudoshnikova.gateway.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ru.gudoshnikova.gateway.dto.FinishRegistrationRequestDto;
import ru.gudoshnikova.gateway.dto.LoanOfferDto;
import ru.gudoshnikova.gateway.dto.LoanStatementRequestDto;
import ru.gudoshnikova.gateway.dto.StatementDto;
import ru.gudoshnikova.gateway.exception.ClientHttpException;
import ru.gudoshnikova.gateway.exception.ExternalServiceException;
import ru.gudoshnikova.gateway.service.GatewayService;
import ru.gudoshnikova.gateway.util.PathConstants;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GatewayServiceImpl implements GatewayService {

    private final RestClient statementRestClient;
    private final RestClient dealRestClient;

    private static final String LOG_START_CREATE_STATEMENT = "Gateway: Creating statement for request: {}";
    private static final String LOG_START_SELECT_OFFER = "Gateway: Selecting offer: {}";
    private static final String LOG_START_CALCULATE_CREDIT = "Gateway: Calculating credit for statement: {}";
    private static final String LOG_START_SEND_DOCUMENTS = "Gateway: Sending documents for statement: {}";
    private static final String LOG_START_SIGN_DOCUMENTS = "Gateway: Signing documents for statement: {}";
    private static final String LOG_START_VERIFY_CODE = "Gateway: Verifying code for statement: {}";
    private static final String LOG_START_GET_STATEMENT_BY_ID = "Gateway: Getting statement by id: {}";
    private static final String LOG_START_GET_ALL_STATEMENTS = "Gateway: Getting all statements";
    private static final String LOG_REQUEST_COMPLETED = "Gateway: Request completed successfully";

    private static final String ERROR_REST_CLIENT = "Gateway: RestClientException while executing '{}', operation: {}";
    private static final String ERROR_SERVER_ERROR = "Gateway: Server error while executing '{}', status code: {}";
    private static final String ERROR_CLIENT_ERROR = "Gateway: Client error while executing '{}', status code: {}";

    private static final String EXTERNAL_SERVICE_ERROR = "Failed to execute operation: {}";
    private static final String INTERNAL_SERVER_ERROR = "Deal or statement service internal error";
    private static final String CLIENT_ERROR_TEMPLATE = "Client error with status code: {}";

    private static final String OP_CREATE_STATEMENT = "create statement";
    private static final String OP_SELECT_OFFER = "select offer";
    private static final String OP_CALCULATE_CREDIT = "calculate credit";
    private static final String OP_SEND_DOCUMENTS = "send documents";
    private static final String OP_SIGN_DOCUMENTS = "sign documents";
    private static final String OP_VERIFY_CODE = "verify code";
    private static final String OP_GET_STATEMENT_BY_ID = "get statement by id";
    private static final String OP_GET_ALL_STATEMENTS = "get all statements";

    @Override
    public List<LoanOfferDto> createStatement(LoanStatementRequestDto request) {
        log.info(LOG_START_CREATE_STATEMENT, request);
        List<LoanOfferDto> offers = executePostForObject(statementRestClient,
                PathConstants.STATEMENT_PATH,
                request,
                new ParameterizedTypeReference<>() {
                },
                OP_CREATE_STATEMENT);
        log.info(LOG_REQUEST_COMPLETED);
        return offers;
    }

    @Override
    public void selectOffer(LoanOfferDto loanOffer) {
        log.info(LOG_START_SELECT_OFFER, loanOffer);
        executePost(statementRestClient,
                PathConstants.STATEMENT_OFFER_PATH,
                loanOffer,
                OP_SELECT_OFFER);
        log.info(LOG_REQUEST_COMPLETED);
    }

    @Override
    public void calculateCredit(UUID statementId, FinishRegistrationRequestDto request) {
        log.info(LOG_START_CALCULATE_CREDIT, statementId);
        executePost(dealRestClient,
                PathConstants.DEAL_CALCULATE_PATH,
                request,
                OP_CALCULATE_CREDIT,
                statementId);
        log.info(LOG_REQUEST_COMPLETED);
    }

    @Override
    public void sendDocuments(UUID statementId) {
        log.info(LOG_START_SEND_DOCUMENTS, statementId);
        executePost(dealRestClient,
                PathConstants.DEAL_DOCUMENT_SEND_PATH,
                null,
                OP_SEND_DOCUMENTS,
                statementId);
        log.info(LOG_REQUEST_COMPLETED);
    }

    @Override
    public void signDocuments(UUID statementId) {
        log.info(LOG_START_SIGN_DOCUMENTS, statementId);
        executePost(dealRestClient,
                PathConstants.DEAL_DOCUMENT_SIGN_PATH,
                null,
                OP_SIGN_DOCUMENTS,
                statementId);
        log.info(LOG_REQUEST_COMPLETED);
    }

    @Override
    public void verifyCode(UUID statementId, String code) {
        log.info(LOG_START_VERIFY_CODE, statementId);
        executePostWithQuery(dealRestClient,
                PathConstants.DEAL_DOCUMENT_CODE_PATH,
                "code",
                code,
                OP_VERIFY_CODE,
                statementId);
        log.info(LOG_REQUEST_COMPLETED);
    }

    @Override
    public StatementDto getStatementById(UUID statementId) {
        log.info(LOG_START_GET_STATEMENT_BY_ID, statementId);
        StatementDto response = executeGet(dealRestClient,
                PathConstants.DEAL_ADMIN_STATEMENT,
                new ParameterizedTypeReference<>() {
                },
                OP_GET_STATEMENT_BY_ID,
                statementId);
        log.info(LOG_REQUEST_COMPLETED);
        return response;
    }

    @Override
    public List<StatementDto> getAllStatements() {
        log.info(LOG_START_GET_ALL_STATEMENTS);
        List<StatementDto> response = executeGet(dealRestClient,
                PathConstants.DEAL_ADMIN_STATEMENTS,
                new ParameterizedTypeReference<>() {
                },
                OP_GET_ALL_STATEMENTS);
        log.info(LOG_REQUEST_COMPLETED);
        return response;
    }

    private void executePost(RestClient client, String uri, Object body,
                             String operationName, Object... uriVariables) {
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

    private void executePostWithQuery(RestClient client, String uri, String queryParam,
                                      String queryValue, String operationName, Object... uriVariables) {
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

    private <T> T executePostForObject(RestClient client, String uri, Object body,
                                       ParameterizedTypeReference<T> responseType,
                                       String operationName, Object... uriVariables) {
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

    private <T> T executeGet(RestClient client, String uri,
                             ParameterizedTypeReference<T> responseType,
                             String operationName, Object... uriVariables) {
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