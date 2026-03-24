package ru.gudoshnikova.calculator.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Запрос на предварительный расчет кредита")
public class LoanStatementRequestDto {

    @NotNull(message = "The loan amount is required")
    @DecimalMin(value = "20000.0", message = "The loan amount must be at least 20,000")
    @DecimalMax(value = "15000000", message = "The loan amount cannot exceed 15000000")
    @Digits(integer = 8, fraction = 2,
            message = "The loan amount must have no more than 8 integers and 2 fractional digits.")
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

    @NotBlank(message = "Email is required")
    @Email(regexp = "^[a-z0-9A-Z_!#$%&'*+/=?`{|}~^.-]+@[a-z0-9A-Z.-]+$",
            message = "Incorrect email format")
    @Schema(description = "Email адрес", example = "ivan.ivanov@example.com")
    private String email;

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
}
