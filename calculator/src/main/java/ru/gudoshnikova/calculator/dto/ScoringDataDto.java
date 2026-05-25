package ru.gudoshnikova.calculator.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;import ru.gudoshnikova.calculator.enums.Gender;import ru.gudoshnikova.calculator.enums.MaritalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Данные для скоринга")
public class ScoringDataDto {

    @NotNull(message = "The loan amount is required")
    @DecimalMin(value = "20000.0", message = "The loan amount must be at least 20,000")
    @Schema(description = "Запрашиваемая сумма кредита", example = "500000.00")
    private BigDecimal amount;

    @NotNull(message = "The loan term is required")
    @Min(value = 6, message = "The loan term must be at least 6 months.")
    @Max(value = 360, message = "The loan term cannot exceed 360 months")
    @Schema(description = "Срок кредита в месяцах", example = "24")
    private Integer term;

    @NotBlank(message = "The firstname is required")
    @Pattern(regexp = "^[a-zA-Z]{2,30}$",
            message = "The name must contain only Latin letters, from 2 to 30 characters")
    @Schema(description = "Имя", example = "Ivan")
    private String firstName;

    @NotBlank(message = "The last name is required")
    @Pattern(regexp = "^[a-zA-Z]{2,30}$",
            message = "The last name must contain only Latin letters, from 2 to 30 characters")
    @Schema(description = "Фамилия", example = "Ivanov")
    private String lastName;

    @Pattern(regexp = "^[a-zA-Z]{2,30}$",
            message = "The middle name must contain only Latin letters, from 2 to 30 characters")
    @Schema(description = "Отчество", example = "Ivanovich")
    private String middleName;

    @NotNull(message = "Gender is required")
    @Schema(description = "Пол", example = "MALE")
    private Gender gender;

    @NotNull(message = "Date of birth is required")
    @Past(message = "The date of birth must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Дата рождения в формате yyyy-MM-dd", example = "1990-01-01")
    private LocalDate birthdate;

    @NotBlank(message = "Passport series is required")
    @Pattern(regexp = "^\\d{4}$", message = "The passport series must consist of 4 digits.")
    @Schema(description = "Серия паспорта (4 цифры)", example = "1234")
    private String passportSeries;

    @NotBlank(message = "The passport number is required")
    @Pattern(regexp = "^\\d{6}$", message = "The passport number must consist of 6 digits.")
    @Schema(description = "Номер паспорта (6 цифр)", example = "567890")
    private String passportNumber;

    @NotNull(message = "The date of issue of the passport is required")
    @Past(message = "The date of issue of the passport must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Дата выдачи паспорта", example = "2010-05-15")
    private LocalDate passportIssueDate;

    @NotBlank(message = "The issue branch of the passport is required")
    @Schema(description = "Код подразделения, выдавшего паспорт", example = "770-001")
    private String passportIssueBranch;

    @NotNull(message = "Marital status is required")
    @Schema(description = "Семейное положение", example = "MARRIED")
    private MaritalStatus maritalStatus;

    @NotNull(message = "The amount of dependents is required")
    @Min(value = 0, message = "The amount of dependents cannot be negative")
    @Schema(description = "Количество иждивенцев", example = "2")
    private Integer dependentAmount;

    @Valid
    @NotNull(message = "Information about employment is required")
    @Schema(description = "Информация о занятости")
    private EmploymentDto employment;

    @NotBlank(message = "The account number is required")
    @Pattern(regexp = "^\\d{20}$", message = "The account number must contain 20 digits.")
    @Schema(description = "Номер счета (20 цифр)", example = "40817810000012345678")
    private String accountNumber;

    @NotNull(message = "The insurance flag is required")
    @Schema(description = "Флаг наличия страховки", example = "true")
    private Boolean isInsuranceEnabled;

    @NotNull(message = "The salary client flag is required")
    @Schema(description = "Флаг зарплатного клиента", example = "true")
    private Boolean isSalaryClient;
}
