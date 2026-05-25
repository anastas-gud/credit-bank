package ru.gudoshnikova.gateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.gudoshnikova.gateway.dto.FinishRegistrationRequestDto;
import ru.gudoshnikova.gateway.dto.LoanOfferDto;
import ru.gudoshnikova.gateway.dto.LoanStatementRequestDto;
import ru.gudoshnikova.gateway.dto.StatementDto;

import java.util.List;
import java.util.UUID;

@RequestMapping("/gateway")
@Tag(name = "API Gateway", description = "Единая точка входа для всех микросервисов")
public interface GatewayController {

    @PostMapping("/statement")
    @Operation(summary = "Создание заявки на кредит",
            description = "Создает заявку и возвращает 4 кредитных предложения")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Заявка успешно создана"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<List<LoanOfferDto>> createStatement(
            @Valid @RequestBody LoanStatementRequestDto request);

    @PostMapping("/statement/offer")
    @Operation(summary = "Выбор кредитного предложения",
            description = "Выбирает одно из предложений и отправляет уведомление")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Предложение успешно выбрано"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "404", description = "Заявка не найдена")
    })
    ResponseEntity<Void> selectOffer(@Valid @RequestBody LoanOfferDto loanOffer);

    @PostMapping("/deal/calculate/{statementId}")
    @Operation(summary = "Полный расчет кредита",
            description = "Завершает регистрацию и выполняет полный расчет кредита")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Кредит успешно рассчитан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации или отказ"),
            @ApiResponse(responseCode = "404", description = "Заявка не найдена")
    })
    ResponseEntity<Void> calculateCredit(
            @Parameter(description = "ID заявки") @PathVariable UUID statementId,
            @Valid @RequestBody FinishRegistrationRequestDto request);

    @PostMapping("/deal/document/{statementId}/send")
    @Operation(summary = "Формирование документов",
            description = "Формирует и отправляет документы на почту")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Документы отправлены"),
            @ApiResponse(responseCode = "404", description = "Заявка не найдена")
    })
    ResponseEntity<Void> sendDocuments(
            @Parameter(description = "ID заявки") @PathVariable UUID statementId);

    @PostMapping("/deal/document/{statementId}/sign")
    @Operation(summary = "Запрос подписания",
            description = "Отправляет код подтверждения для подписания")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Код отправлен"),
            @ApiResponse(responseCode = "404", description = "Заявка не найдена")
    })
    ResponseEntity<Void> signDocuments(
            @Parameter(description = "ID заявки") @PathVariable UUID statementId);

    @PostMapping("/deal/document/{statementId}/code")
    @Operation(summary = "Подтверждение кода",
            description = "Проверяет код и выдает кредит")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Код подтвержден"),
            @ApiResponse(responseCode = "400", description = "Неверный код"),
            @ApiResponse(responseCode = "404", description = "Заявка не найдена")
    })
    ResponseEntity<Void> verifyCode(
            @Parameter(description = "ID заявки") @PathVariable UUID statementId,
            @Parameter(description = "Код подтверждения") @RequestParam String code);

    @GetMapping("/deal/admin/statement/{statementId}")
    @Operation(summary = "Получение заявки по ID (Admin)",
            description = "Админский метод для получения заявки по идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Заявка найдена"),
            @ApiResponse(responseCode = "404", description = "Заявка не найдена")
    })
    ResponseEntity<StatementDto> getStatementById(
            @Parameter(description = "ID заявки") @PathVariable UUID statementId);

    @GetMapping("/deal/admin/statement")
    @Operation(summary = "Получение всех заявок (Admin)",
            description = "Админский метод для получения списка всех заявок")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список заявок получен")
    })
    ResponseEntity<List<StatementDto>> getAllStatements();
}
