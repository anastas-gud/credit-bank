package ru.gudoshnikova.deal.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import ru.gudoshnikova.deal.dto.FinishRegistrationRequestDto;
import ru.gudoshnikova.deal.dto.LoanOfferDto;
import ru.gudoshnikova.deal.dto.LoanStatementRequestDto;
import ru.gudoshnikova.deal.dto.StatementStatusHistoryDto;
import ru.gudoshnikova.deal.enums.ApplicationStatus;
import ru.gudoshnikova.deal.enums.ChangeType;
import ru.gudoshnikova.deal.config.RestClientConfig;
import ru.gudoshnikova.deal.dto.CreditDto;
import ru.gudoshnikova.deal.dto.ScoringDataDto;
import ru.gudoshnikova.deal.exception.NotFoundException;
import ru.gudoshnikova.deal.mapper.ClientMapper;
import ru.gudoshnikova.deal.mapper.CreditMapper;
import ru.gudoshnikova.deal.mapper.ScoringDataMapper;
import ru.gudoshnikova.deal.model.Client;
import ru.gudoshnikova.deal.model.Credit;
import ru.gudoshnikova.deal.model.Statement;
import ru.gudoshnikova.deal.repository.ClientRepository;
import ru.gudoshnikova.deal.repository.CreditRepository;
import ru.gudoshnikova.deal.repository.StatementRepository;
import ru.gudoshnikova.deal.service.DealService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class DealServiceImpl implements DealService {

    private final ClientRepository clientRepository;
    private final StatementRepository statementRepository;
    private final CreditRepository creditRepository;
    private final RestClientConfig restClient;

    private final ClientMapper clientMapper;
    private final ScoringDataMapper scoringDataMapper;
    private final CreditMapper creditMapper;

    @Transactional
    @Override
    public List<LoanOfferDto> createStatement(LoanStatementRequestDto request) {
        log.info("Creating statement for request: {}", request);

        Client client = clientMapper.toClient(request);
        clientRepository.save(client);
        log.info("Client saved with id: {}", client.getClientId());

        Statement statement = Statement.builder()
                .client(client)
                .creationDate(LocalDateTime.now())
                .status(ApplicationStatus.PREAPPROVAL)
                .build();
        if (statement.getStatusHistory() == null) {
            statement.setStatusHistory(new ArrayList<>());
        }
        statement.getStatusHistory().add(StatementStatusHistoryDto.builder()
                .status(ApplicationStatus.PREAPPROVAL)
                .time(LocalDateTime.now())
                .changeType(ChangeType.AUTOMATIC)
                .build());
        statementRepository.save(statement);
        log.info("Statement saved with id: {}", statement.getStatementId());

        log.info("Sending request to calculator service for offers");
        List<LoanOfferDto> offers = restClient.calculatorRestClient()
                .post()
                .uri("/calculator/offers")
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        log.info("Received {} offers from calculator", offers.size());

        offers.forEach(offer -> offer.setStatementId(statement.getStatementId()));
        log.debug("Assigned statementId {} to all offers", statement.getStatementId());

        return offers;
    }

    @Transactional
    @Override
    public void selectOffer(LoanOfferDto offer) {
        log.info("Selecting offer: {}", offer);

        Statement statement = statementRepository.findById(offer.getStatementId())
                .orElseThrow(() -> new NotFoundException("Statement not found with id: "
                        + offer.getStatementId()));
        log.info("Found statement with id: {}", statement.getStatementId());

        statement.setStatus(ApplicationStatus.APPROVED);

        statement.getStatusHistory().add(StatementStatusHistoryDto.builder()
                .status(ApplicationStatus.APPROVED)
                .time(LocalDateTime.now())
                .changeType(ChangeType.AUTOMATIC)
                .build());

        statement.setAppliedOffer(offer);
        log.debug("Applied offer saved: {}", offer);

        statementRepository.save(statement);
        log.info("Offer selected successfully for statement: {}", statement.getStatementId());
    }

    @Transactional
    @Override
    public void calculateCredit(UUID statementId, FinishRegistrationRequestDto request) {
        log.info("Calculating credit for statement: {}", statementId);

        Statement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new NotFoundException("Statement not found with id: "
                        + statementId));

        log.info("Found statement with id: {}", statement.getStatementId());

        Client client = statement.getClient();
        clientMapper.updateClientFromFinishRegistration(request, client);
        clientRepository.save(client);
        log.info("Client updated with id: {}", client.getClientId());

        ScoringDataDto scoringDataDto = scoringDataMapper.toScoringDataDto(client, statement);
        log.debug("Scoring data built: {}", scoringDataDto);

        log.info("Sending request to calculator service for credit calculation");
        CreditDto creditDto = restClient.calculatorRestClient().post()
                .uri("/calculator/calc")
                .body(scoringDataDto)
                .retrieve()
                .body(CreditDto.class);
        log.info("Received credit calculation from calculator");

        Credit credit = creditMapper.toCredit(creditDto, statement);
        creditRepository.save(credit);
        log.info("Credit saved with id: {}", credit.getCreditId());

        statement.setStatus(ApplicationStatus.CC_APPROVED);
        statement.setCredit(credit);

        statement.getStatusHistory().add(StatementStatusHistoryDto.builder()
                .status(ApplicationStatus.CC_APPROVED)
                .time(LocalDateTime.now())
                .changeType(ChangeType.AUTOMATIC)
                .build());

        statementRepository.save(statement);
        log.info("Credit calculation completed successfully for statement: {}", statementId);
    }
}
