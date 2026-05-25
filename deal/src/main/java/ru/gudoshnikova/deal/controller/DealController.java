package ru.gudoshnikova.deal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.gudoshnikova.deal.dto.ErrorResponseDto;
import ru.gudoshnikova.deal.dto.FinishRegistrationRequestDto;
import ru.gudoshnikova.deal.dto.LoanOfferDto;
import ru.gudoshnikova.deal.dto.LoanStatementRequestDto;

import java.util.List;
import java.util.UUID;

@RequestMapping("/deal")
@Tag(name = "Deal Controller", description = "Контроллер для управления заявками и кредитами")
public interface DealController {
    @PostMapping("/statement")
    @Operation(
            summary = "Создание заявки на кредит",
            description = "Создает клиента и заявку, отправляет запрос в калькулятор для расчета предложений"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное создание заявки",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = LoanOfferDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации входных данных",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content()
            )
    })
    ResponseEntity<List<LoanOfferDto>> createStatement(
            @Parameter(
                    description = "Данные заявки на кредит",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoanStatementRequestDto.class)
                    )
            )
            @Valid @RequestBody LoanStatementRequestDto request);

    @PostMapping("/offer/select")
    @Operation(
            summary = "Выбор кредитного предложения",
            description = "Выбирает одно из предложений и сохраняет его в заявке"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Предложение успешно выбрано",
                    content = @Content()
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации входных данных",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Заявка не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content()
            )
    })
    ResponseEntity<Void> selectOffer(
            @Parameter(
                    description = "Кредитное предложение",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoanOfferDto.class)
                    )
            )
            @Valid @RequestBody LoanOfferDto loanOffer);

    @PostMapping("/calculate/{statementId}")
    @Operation(
            summary = "Полный расчет кредита",
            description = "Завершает регистрацию и выполняет полный расчет кредита"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Кредит успешно рассчитан",
                    content = @Content()
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации входных данных",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Заявка не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content()
            )
    })
    ResponseEntity<Void> calculateCredit(
            @PathVariable UUID statementId,
            @Parameter(
                    description = "Запрос на завершение регистрации и полного расчета кредита",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FinishRegistrationRequestDto.class)
                    )
            )
            @Valid @RequestBody FinishRegistrationRequestDto request);

    @PostMapping("/document/{statementId}/send")
    @Operation(
            summary = "Запрос на отправку документов",
            description = "Формирует документы и отправляет их на почту клиенту"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Документы отправлены"),
            @ApiResponse(responseCode = "404", description = "Заявка не найдена"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<Void> sendDocuments(@PathVariable UUID statementId);

    @PostMapping("/document/{statementId}/sign")
    @Operation(
            summary = "Запрос на подписание документов",
            description = "Отправляет код подтверждения для подписания документов"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Код подтверждения отправлен"),
            @ApiResponse(responseCode = "404", description = "Заявка не найдена"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<Void> signDocuments(@PathVariable UUID statementId);

    @PostMapping("/document/{statementId}/code")
    @Operation(
            summary = "Подписание документов",
            description = "Подтверждает код и выдает кредит"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Кредит успешно выдан"),
            @ApiResponse(responseCode = "404", description = "Заявка не найдена"),
            @ApiResponse(responseCode = "400", description = "Неверный код подтверждения"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<Void> verifyCode(@PathVariable UUID statementId, @RequestParam String code);
}
