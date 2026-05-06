package ru.gudoshnikova.deal.service;

import ru.gudoshnikova.deal.dto.StatementDto;

import java.util.List;
import java.util.UUID;

public interface DealAdminService {
    StatementDto getStatementById(UUID statementId);

    List<StatementDto> getAllStatements();
}
