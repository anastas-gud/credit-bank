package ru.gudoshnikova.calculator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.gudoshnikova.calculator.enums.EmploymentStatus;
import ru.gudoshnikova.calculator.enums.Position;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Информация о занятости")
public class EmploymentDto {

    @NotNull(message = "Employment status is required")
    @Schema(description = "Статус занятости", example = "EMPLOYED")
    private EmploymentStatus employmentStatus;

    @NotBlank(message = "The INN of the employer is required")
    @Pattern(regexp = "^\\d{10}$|^\\d{12}$", message = "The INN must contain 10 or 12 digits.")
    @Schema(description = "ИНН работодателя", example = "7701234567")
    private String employerINN;

    @NotNull(message = "Salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "The salary must be more than 0")
    @Schema(description = "Зарплата", example = "100000.00")
    private BigDecimal salary;

    @NotNull(message = "The position is required")
    @Schema(description = "Должность", example = "MIDDLE_MANAGER")
    private Position position;

    @NotNull(message = "Total work experience is required")
    @Min(value = 0, message = "Total work experience cannot be negative")
    @Schema(description = "Общий стаж работы в месяцах", example = "60")
    private Integer workExperienceTotal;

    @NotNull(message = "Current work experience is required")
    @Min(value = 0, message = "Current work experience cannot be negative")
    @Schema(description = "Текущий стаж работы в месяцах", example = "24")
    private Integer workExperienceCurrent;
}
