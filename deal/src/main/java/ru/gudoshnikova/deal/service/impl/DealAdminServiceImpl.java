package ru.gudoshnikova.deal.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gudoshnikova.deal.dto.StatementDto;
import ru.gudoshnikova.deal.exception.NotFoundException;
import ru.gudoshnikova.deal.model.Statement;
import ru.gudoshnikova.deal.repository.StatementRepository;
import ru.gudoshnikova.deal.service.DealAdminService;
import ru.gudoshnikova.deal.util.ExceptionMessageConstants;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealAdminServiceImpl implements DealAdminService {

    private final StatementRepository statementRepository;
    private final ObjectMapper objectMapper;

    @Override
    public StatementDto getStatementById(UUID statementId) {
        log.info("Getting statement by id: {}", statementId);
        Statement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new NotFoundException(
                        String.format(ExceptionMessageConstants.STATEMENT_NOT_FOUND_MSG, statementId)));
        return convertToDto(statement);
    }

    @Override
    public List<StatementDto> getAllStatements() {
        log.info("Getting all statements");
        List<Statement> statements = statementRepository.findAll();
        log.info("Found {} statements", statements.size());
        return statements.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private StatementDto convertToDto(Statement statement) {
        if (statement == null) {
            return null;
        }

        StatementDto dto = objectMapper.convertValue(statement, StatementDto.class);
        if (statement.getClient() != null) {
            dto.setClientId(statement.getClient().getClientId());
        }
        if (statement.getCredit() != null) {
            dto.setCreditId(statement.getCredit().getCreditId());
        }

        return dto;
    }
}
