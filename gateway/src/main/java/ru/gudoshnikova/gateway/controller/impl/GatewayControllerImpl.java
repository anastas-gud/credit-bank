package ru.gudoshnikova.gateway.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.gudoshnikova.gateway.controller.GatewayController;
import ru.gudoshnikova.gateway.dto.FinishRegistrationRequestDto;
import ru.gudoshnikova.gateway.dto.LoanOfferDto;
import ru.gudoshnikova.gateway.dto.LoanStatementRequestDto;
import ru.gudoshnikova.gateway.dto.StatementDto;
import ru.gudoshnikova.gateway.service.GatewayService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GatewayControllerImpl implements GatewayController {

    private final GatewayService gatewayService;

    @Override
    public ResponseEntity<List<LoanOfferDto>> createStatement(LoanStatementRequestDto request) {
        log.info("API Gateway - POST /api/statement");
        List<LoanOfferDto> offers = gatewayService.createStatement(request);
        return ResponseEntity.ok(offers);
    }

    @Override
    public ResponseEntity<Void> selectOffer(LoanOfferDto loanOffer) {
        log.info("API Gateway - POST /api/statement/offer");
        gatewayService.selectOffer(loanOffer);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> calculateCredit(UUID statementId, FinishRegistrationRequestDto request) {
        log.info("API Gateway - POST /api/deal/calculate/{}", statementId);
        gatewayService.calculateCredit(statementId, request);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> sendDocuments(UUID statementId) {
        log.info("API Gateway - POST /api/deal/document/{}/send", statementId);
        gatewayService.sendDocuments(statementId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> signDocuments(UUID statementId) {
        log.info("API Gateway - POST /api/deal/document/{}/sign", statementId);
        gatewayService.signDocuments(statementId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> verifyCode(UUID statementId, String code) {
        log.info("API Gateway - POST /api/deal/document/{}/code", statementId);
        gatewayService.verifyCode(statementId, code);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<StatementDto> getStatementById(UUID statementId) {
        log.info("API Gateway - GET /api/deal/admin/statement/{}", statementId);
        StatementDto statement = gatewayService.getStatementById(statementId);
        return ResponseEntity.ok(statement);
    }

    @Override
    public ResponseEntity<List<StatementDto>> getAllStatements() {
        log.info("API Gateway - GET /api/deal/admin/statement");
        List<StatementDto> statements = gatewayService.getAllStatements();
        return ResponseEntity.ok(statements);
    }
}
