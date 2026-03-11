package ru.gudoshnikova.calculator.controller;

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
import org.springframework.web.bind.annotation.RequestBody;
import ru.gudoshnikova.calculator.dto.CreditDto;
import ru.gudoshnikova.calculator.dto.ErrorResponseDto;
import ru.gudoshnikova.calculator.dto.LoanOfferDto;
import ru.gudoshnikova.calculator.dto.LoanStatementRequestDto;
import ru.gudoshnikova.calculator.dto.ScoringDataDto;

import java.util.List;

@Tag(name = "Calculator Controller", description = "Контроллер для расчета кредитных предложений и кредита")
public interface CalculatorController {

    @Operation(
            summary = "Расчет возможных условий кредита",
            description = """
                    На основании заявки рассчитывает 4 кредитных предложения с различными комбинациями:
                    * Без страховки, не зарплатный клиент
                    * Без страховки, зарплатный клиент
                    * Со страховкой, не зарплатный клиент
                    * Со страховкой, зарплатный клиент
                    
                    Предложения сортируются от худшего к лучшему (по возрастанию процентной ставки)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешный расчет предложений",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = LoanOfferDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации входных данных или прескоринга",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    ResponseEntity<List<LoanOfferDto>> calculateOffers(
            @Parameter(
                    description = "Данные заявки на кредит",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoanStatementRequestDto.class)
                    )
            )
            @Valid @RequestBody LoanStatementRequestDto request
    );


    @Operation(
            summary = "Полный расчет параметров кредита",
            description = """
                    Выполняет скоринг данных и полный расчет параметров кредита:
                    * Проверка возраста, стажа, дохода
                    * Корректировка процентной ставки
                    * Расчет ежемесячного платежа
                    * Расчет графика платежей
                    * Расчет ПСК (полной стоимости кредита)
                    
                    В случае несоответствия критериям возвращает отказ с описанием причины
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешный расчет кредита",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreditDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации данных, скоринга или отказ в кредите",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    ResponseEntity<CreditDto> calculateCredit(
            @Parameter(
                    description = "Данные для скоринга",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ScoringDataDto.class)
                    )
            )
            @Valid @RequestBody ScoringDataDto scoringData
    );
}