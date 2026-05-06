package ru.gudoshnikova.deal.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.gudoshnikova.deal.controller.DealAdminController;
import ru.gudoshnikova.deal.dto.StatementDto;
import ru.gudoshnikova.deal.service.DealAdminService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DealAdminControllerImpl implements DealAdminController {

    private final DealAdminService dealAdminService;

    @Override
    public ResponseEntity<StatementDto> getStatementById(UUID statementId) {
        log.info("GET /deal/admin/statement/{} - Getting statement by id", statementId);
        StatementDto statement = dealAdminService.getStatementById(statementId);
        return ResponseEntity.ok(statement);
    }

    @Override
    public ResponseEntity<List<StatementDto>> getAllStatements() {
        log.info("GET /deal/admin/statement - Getting all statements");
        List<StatementDto> statements = dealAdminService.getAllStatements();
        log.info("Found {} statements", statements.size());
        return ResponseEntity.ok(statements);
    }
}
