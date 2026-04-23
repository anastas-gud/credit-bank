package ru.gudoshnikova.deal.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.gudoshnikova.deal.dto.EmailMessage;
import ru.gudoshnikova.deal.dto.FinishRegistrationRequestDto;
import ru.gudoshnikova.deal.dto.LoanOfferDto;
import ru.gudoshnikova.deal.dto.LoanStatementRequestDto;
import ru.gudoshnikova.deal.dto.StatementStatusHistoryDto;
import ru.gudoshnikova.deal.enums.ApplicationStatus;
import ru.gudoshnikova.deal.enums.ChangeType;
import ru.gudoshnikova.deal.dto.CreditDto;
import ru.gudoshnikova.deal.dto.ScoringDataDto;
import ru.gudoshnikova.deal.enums.CreditStatus;
import ru.gudoshnikova.deal.enums.Theme;
import ru.gudoshnikova.deal.exception.CalculatorServiceException;
import ru.gudoshnikova.deal.exception.NotFoundException;
import ru.gudoshnikova.deal.integration.calculator.service.CalculatorService;
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
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class DealServiceImpl implements DealService {

    private final ClientRepository clientRepository;
    private final StatementRepository statementRepository;
    private final CreditRepository creditRepository;
    private final CalculatorService calculatorService;

    private final ClientMapper clientMapper;
    private final ScoringDataMapper scoringDataMapper;
    private final CreditMapper creditMapper;

    private final KafkaTemplate<String, EmailMessage> kafkaTemplate;
    private final DocumentGeneratorServiceImpl documentGenerator;

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
        List<LoanOfferDto> offers = calculatorService.sendOffersRequest(request);
        log.info("Received {} offers from calculator", offers.size());

        offers.forEach(offer -> offer.setStatementId(statement.getStatementId()));
        log.debug("Assigned statementId {} to all offers", statement.getStatementId());

        return offers;
    }

    @Transactional
    @Override
    public void selectOffer(LoanOfferDto offer) {
        log.info("Selecting offer: {}", offer);

        Statement statement = statementRepository.findByIdWithLock(offer.getStatementId())
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

        sendFinishRegistrationEmail(statement);
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

        try {
            log.info("Sending request to calculator service for credit calculation");
            CreditDto creditDto = calculatorService.sendCalculateRequest(scoringDataDto);
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

            sendScoringResultEmail(statement);
            log.info("Credit calculation completed successfully for statement: {}", statementId);
        } catch (CalculatorServiceException e) {
            log.error("Calculator service returned error: type={}, message={}", e.getErrorType(), e.getMessage());

            statement.setStatus(ApplicationStatus.CC_DENIED);
            statement.getStatusHistory().add(StatementStatusHistoryDto.builder()
                    .status(ApplicationStatus.CC_DENIED)
                    .time(LocalDateTime.now())
                    .changeType(ChangeType.AUTOMATIC)
                    .build());
            statementRepository.save(statement);

            sendStatementDeniedEmail(statement, e.getMessage());

            log.info("Credit denied for statement: {}", statementId);
        }
    }

    @Override
    @Transactional
    public void sendDocuments(UUID statementId) {
        log.info("Sending documents for statement: {}", statementId);

        Statement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new NotFoundException("Statement not found with id: " + statementId));

        statement.setStatus(ApplicationStatus.PREPARE_DOCUMENTS);
        statement.getStatusHistory().add(StatementStatusHistoryDto.builder()
                .status(ApplicationStatus.PREPARE_DOCUMENTS)
                .time(LocalDateTime.now())
                .changeType(ChangeType.AUTOMATIC)
                .build());
        statementRepository.save(statement);

        byte[] document = documentGenerator.generateCreditDocument(
                statementId,
                statement.getClient().getFirstName() + " " + statement.getClient().getLastName(),
                statement.getAppliedOffer().getTotalAmount().doubleValue(),
                statement.getAppliedOffer().getTerm(),
                statement.getAppliedOffer().getRate().doubleValue()
        );

        sendDocumentsEmail(statement, document);
        log.info("Documents sent for statement: {}", statementId);
    }

    @Override
    @Transactional
    public void signDocuments(UUID statementId) {
        log.info("Signing documents for statement: {}", statementId);

        Statement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new NotFoundException("Statement not found with id: " + statementId));

        String sesCode = String.format("%06d", new Random().nextInt(999999));
        statement.setSesCode(sesCode);
        statementRepository.save(statement);

        sendSesCodeEmail(statement, sesCode);
        log.info("SES code sent for statement: {}", statementId);
    }

    @Override
    @Transactional
    public void verifyCode(UUID statementId, String code) {
        log.info("Verifying code for statement: {}", statementId);

        Statement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new NotFoundException("Statement not found with id: " + statementId));

        if (statement.getSesCode() != null && statement.getSesCode().equals(code)) {
            Credit credit = statement.getCredit();
            credit.setCreditStatus(CreditStatus.ISSUED);
            creditRepository.save(credit);

            statement.setStatus(ApplicationStatus.CREDIT_ISSUED);
            statement.setSignDate(LocalDateTime.now());
            statement.getStatusHistory().add(StatementStatusHistoryDto.builder()
                    .status(ApplicationStatus.CREDIT_ISSUED)
                    .time(LocalDateTime.now())
                    .changeType(ChangeType.AUTOMATIC)
                    .build());
            statementRepository.save(statement);

            sendCreditIssuedEmail(statement);
            log.info("Credit issued successfully for statement: {}", statementId);
        } else {
            log.warn("Invalid SES code for statement: {}", statementId);
            throw new IllegalArgumentException("Invalid verification code");
        }
    }

    private void sendFinishRegistrationEmail(Statement statement) {
        EmailMessage emailMessage = EmailMessage.builder()
                .address(statement.getClient().getEmail())
                .theme(Theme.FINISH_REGISTRATION)
                .statementId(statement.getStatementId())
                .text("")
                .build();
        kafkaTemplate.send("finish-registration", emailMessage);
        log.info("Finish registration email sent to Kafka for statement: {}", statement.getStatementId());
    }

    private void sendScoringResultEmail(Statement statement) {
        EmailMessage emailMessage = EmailMessage.builder()
                .address(statement.getClient().getEmail())
                .theme(Theme.CREATE_DOCUMENTS)
                .statementId(statement.getStatementId())
                .text("Ваша заявка одобрена")
                .build();
        kafkaTemplate.send("create-documents", emailMessage);
        log.info("Scoring result email sent to Kafka for statement: {}", statement.getStatementId());
    }

    private void sendStatementDeniedEmail(Statement statement, String reason) {
        EmailMessage emailMessage = EmailMessage.builder()
                .address(statement.getClient().getEmail())
                .theme(Theme.STATEMENT_DENIED)
                .statementId(statement.getStatementId())
                .text(reason)
                .build();
        kafkaTemplate.send("statement-denied", emailMessage);
        log.info("Statement denied email sent to Kafka for statement: {}", statement.getStatementId());
    }

    private void sendDocumentsEmail(Statement statement, byte[] document) {
        EmailMessage emailMessage = EmailMessage.builder()
                .address(statement.getClient().getEmail())
                .theme(Theme.SEND_DOCUMENTS)
                .statementId(statement.getStatementId())
                .document(document)
                .text("")
                .build();
        kafkaTemplate.send("send-documents", emailMessage);
        log.info("Documents email sent to Kafka for statement: {}", statement.getStatementId());
    }

    private void sendSesCodeEmail(Statement statement, String sesCode) {
        EmailMessage emailMessage = EmailMessage.builder()
                .address(statement.getClient().getEmail())
                .theme(Theme.SEND_SES)
                .statementId(statement.getStatementId())
                .text(sesCode)
                .build();
        kafkaTemplate.send("send-ses", emailMessage);
        log.info("SES code email sent to Kafka for statement: {}", statement.getStatementId());
    }

    private void sendCreditIssuedEmail(Statement statement) {
        EmailMessage emailMessage = EmailMessage.builder()
                .address(statement.getClient().getEmail())
                .theme(Theme.CREDIT_ISSUED)
                .statementId(statement.getStatementId())
                .text("")
                .build();
        kafkaTemplate.send("credit-issued", emailMessage);
        log.info("Credit issued email sent to Kafka for statement: {}", statement.getStatementId());
    }
}
