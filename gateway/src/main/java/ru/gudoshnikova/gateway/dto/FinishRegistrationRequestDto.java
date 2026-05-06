package ru.gudoshnikova.gateway.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
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
import ru.gudoshnikova.gateway.enums.Gender;
import ru.gudoshnikova.gateway.enums.MaritalStatus;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на завершение регистрации и полного расчета кредита")
public class FinishRegistrationRequestDto {
    @Schema(description = "Пол клиента", example = "MALE")
    @NotNull(message = "Пол обязателен для заполнения")
    private Gender gender;

    @Schema(description = "Семейное положение", example = "MARRIED")
    @NotNull(message = "Семейное положение обязательно для заполнения")
    private MaritalStatus maritalStatus;

    @Schema(description = "Количество иждивенцев", example = "2")
    @NotNull(message = "Количество иждивенцев обязательно для заполнения")
    @Min(value = 0, message = "Количество иждивенцев не может быть отрицательным")
    @Max(value = 10, message = "Количество иждивенцев не может превышать 10")
    private Integer dependentAmount;

    @Schema(description = "Дата выдачи паспорта", example = "2010-05-15")
    @NotNull(message = "Дата выдачи паспорта обязательна")
    @Past(message = "Дата выдачи паспорта должна быть в прошлом")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate passportIssueDate;

    @Schema(description = "Код подразделения, выдавшего паспорт", example = "770-001")
    @NotBlank(message = "Код подразделения обязателен")
    @Pattern(regexp = "^\\d{3}-\\d{3}$",
            message = "Код подразделения должен быть в формате XXX-XXX, где X - цифра")
    private String passportIssueBranch;

    @Schema(description = "Информация о занятости")
    @Valid
    @NotNull(message = "Информация о занятости обязательна")
    private EmploymentDto employment;

    @Schema(description = "Номер счета для зачисления кредита", example = "40817810000012345678")
    @NotBlank(message = "Номер счета обязателен")
    @Pattern(regexp = "^\\d{20}$",
            message = "Номер счета должен содержать ровно 20 цифр")
    private String accountNumber;
}
