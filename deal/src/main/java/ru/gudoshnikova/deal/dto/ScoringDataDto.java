package ru.gudoshnikova.deal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.gudoshnikova.deal.enums.Gender;
import ru.gudoshnikova.deal.enums.MaritalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Данные для скоринга")
public class ScoringDataDto {
    @Schema(description = "Запрашиваемая сумма кредита", example = "500000.00")
    private BigDecimal amount;

    @Schema(description = "Срок кредита в месяцах", example = "24")
    private Integer term;

    @Schema(description = "Имя", example = "Ivan")
    private String firstName;

    @Schema(description = "Фамилия", example = "Ivanov")
    private String lastName;

    @Schema(description = "Отчество", example = "Ivanovich")
    private String middleName;

    @Schema(description = "Пол", example = "MALE")
    private Gender gender;

    @Schema(description = "Дата рождения в формате yyyy-MM-dd", example = "1990-01-01")
    private LocalDate birthdate;

    @Schema(description = "Серия паспорта (4 цифры)", example = "1234")
    private String passportSeries;

    @Schema(description = "Номер паспорта (6 цифр)", example = "567890")
    private String passportNumber;

    @Schema(description = "Дата выдачи паспорта", example = "2010-05-15")
    private LocalDate passportIssueDate;

    @Schema(description = "Код подразделения, выдавшего паспорт", example = "770-001")
    private String passportIssueBranch;

    @Schema(description = "Семейное положение", example = "MARRIED")
    private MaritalStatus maritalStatus;

    @Schema(description = "Количество иждивенцев", example = "2")
    private Integer dependentAmount;

    @Schema(description = "Информация о занятости")
    private EmploymentDto employment;

    @Schema(description = "Номер счета (20 цифр)", example = "40817810000012345678")
    private String accountNumber;

    @Schema(description = "Флаг наличия страховки", example = "true")
    private Boolean isInsuranceEnabled;

    @Schema(description = "Флаг зарплатного клиента", example = "true")
    private Boolean isSalaryClient;
}
