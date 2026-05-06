package ru.gudoshnikova.deal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.gudoshnikova.deal.dto.ErrorResponseDto;
import ru.gudoshnikova.deal.dto.StatementDto;

import java.util.List;
import java.util.UUID;

@RequestMapping("/deal/admin")
@Tag(name = "Deal Admin Controller", description = "Админские API для управления заявками")
public interface DealAdminController {

    @GetMapping("/statement/{statementId}")
    @Operation(summary = "Получение заявки по ID",
            description = "Возвращает полную информацию о заявке по её идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Заявка найдена",
                    content = @Content(schema = @Schema(implementation = StatementDto.class))),
            @ApiResponse(responseCode = "404", description = "Заявка не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<StatementDto> getStatementById(
            @Parameter(description = "ID заявки", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID statementId);

    @GetMapping("/statement")
    @Operation(summary = "Получение всех заявок",
            description = "Возвращает список всех заявок в системе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список заявок получен"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<List<StatementDto>> getAllStatements();
}
