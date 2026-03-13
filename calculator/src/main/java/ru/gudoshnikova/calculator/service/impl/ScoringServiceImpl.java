package ru.gudoshnikova.calculator.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gudoshnikova.calculator.config.CalculatorConfig;
import ru.gudoshnikova.calculator.dto.EmploymentDto;
import ru.gudoshnikova.calculator.dto.ScoringDataDto;
import ru.gudoshnikova.calculator.enums.EmploymentStatus;
import ru.gudoshnikova.calculator.enums.Gender;
import ru.gudoshnikova.calculator.enums.MaritalStatus;
import ru.gudoshnikova.calculator.enums.Position;
import ru.gudoshnikova.calculator.exception.LoanDeniedException;
import ru.gudoshnikova.calculator.service.ScoringService;
import ru.gudoshnikova.calculator.util.ScoringConstants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoringServiceImpl implements ScoringService {
    private final CalculatorConfig config;

    @Override
    public BigDecimal score(ScoringDataDto scoringData) {
        log.info("The beginning of data scoring: {}", scoringData);

        log.debug("Age verification");
        checkAge(scoringData.getBirthdate());
        log.debug("Age verification completed successfully");

        log.debug("Employment check");
        checkEmployment(scoringData.getEmployment());
        log.debug("Employment verification completed successfully");

        log.debug("Checking the loan-to-salary ratio");
        checkLoanToSalary(scoringData.getAmount(), scoringData.getEmployment().getSalary());
        log.debug("Verification of the loan-to-salary ratio has been successfully completed");

        log.debug("Checking work experience");
        checkWorkExperience(scoringData.getEmployment());
        log.debug("The work experience check has been completed successfully");


        BigDecimal rate = config.getBaseRate();
        log.debug("Base rate: {}%", rate);

        log.debug("The beginning of the recalculation of the loan rate");
        rate = recalculationForEmploymentStatus(rate, scoringData.getEmployment().getEmploymentStatus());
        rate = recalculationForPosition(rate, scoringData.getEmployment().getPosition());
        rate = recalculationForMaritalStatus(rate, scoringData.getMaritalStatus());
        rate = recalculationForGenderAndAge(rate, scoringData.getGender(), scoringData.getBirthdate());
        rate = recalculationForServices(rate, scoringData.getIsInsuranceEnabled(), scoringData.getIsSalaryClient());

        log.info("Final interest rate: {}%", rate.setScale(2, RoundingMode.HALF_UP));

        return rate;
    }

    private void checkAge(LocalDate birthDate) {
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        log.debug("Client's age: {} years", age);
        if (age < ScoringConstants.MIN_AGE) {
            log.warn("Denial: age {} is less than the minimum age {}", age, ScoringConstants.MIN_AGE);
            throw new LoanDeniedException(
                    String.format("The client is too young. Minimum age: %d years (current: %d)",
                            ScoringConstants.MIN_AGE, age));
        }
        if (age > ScoringConstants.MAX_AGE) {
            log.warn("Denial: age {} is over the maximum age {}", age, ScoringConstants.MAX_AGE);
            throw new LoanDeniedException(
                    String.format("The client is too old. Maximum age: %d years (current: %d)",
                            ScoringConstants.MAX_AGE, age));
        }
    }

    private void checkEmployment(EmploymentDto employment) {
        log.debug("Employment status: {}", employment.getEmploymentStatus());
        if (employment.getEmploymentStatus() == EmploymentStatus.UNEMPLOYED) {
            log.warn("Denial: Employment status is 'Unemployed'");
            throw new LoanDeniedException("A loan cannot be given to the unemployed");
        }
    }

    private void checkLoanToSalary(BigDecimal amount, BigDecimal salary) {
        log.debug("Loan amount: {}, Salary: {}", amount, salary);
        BigDecimal maxAllowedLoan = salary.multiply(BigDecimal.valueOf(ScoringConstants.MAX_LOAN_TO_SALARY_RATIO));

        if (amount.compareTo(maxAllowedLoan) > 0) {
            log.warn("Denial: the loan amount exceeds {} salaries", ScoringConstants.MAX_LOAN_TO_SALARY_RATIO);
            throw new LoanDeniedException(
                    String.format("The loan amount should not exceed %d of the salary." +
                                    "Maximum allowed: %.2f, requested: %.2f",
                            ScoringConstants.MAX_LOAN_TO_SALARY_RATIO, maxAllowedLoan, amount));
        }
    }

    private void checkWorkExperience(EmploymentDto employment) {
        log.debug("Total experience: {} months, Current experience: {} months",
                employment.getWorkExperienceTotal(), employment.getWorkExperienceCurrent());
        if (employment.getWorkExperienceTotal() < ScoringConstants.MIN_TOTAL_EXPERIENCE) {
            log.warn("Denial: total experience {} months is less than the minimum {}",
                    employment.getWorkExperienceTotal(), ScoringConstants.MIN_TOTAL_EXPERIENCE);
            throw new LoanDeniedException(
                    String.format("The total experience must be at least %d months (current: %d)",
                            ScoringConstants.MIN_TOTAL_EXPERIENCE, employment.getWorkExperienceTotal()));
        }
        if (employment.getWorkExperienceCurrent() < ScoringConstants.MIN_CURRENT_EXPERIENCE) {
            log.warn("Denial: the current experience is {} months less than the minimum {}",
                    employment.getWorkExperienceCurrent(), ScoringConstants.MIN_CURRENT_EXPERIENCE);
            throw new LoanDeniedException(
                    String.format("Current work experience must be at least %d months (current: %d)",
                            ScoringConstants.MIN_CURRENT_EXPERIENCE, employment.getWorkExperienceCurrent()));
        }
    }

    private BigDecimal recalculationForEmploymentStatus(BigDecimal rate, EmploymentStatus status) {
        log.debug("Recalculation by employment status: {}", status);
        switch (status) {
            case SELF_EMPLOYED:
                rate = rate.add(ScoringConstants.SELF_EMPLOYED_INCREASE);
                log.debug("Self-employed: rate increased by {}%", ScoringConstants.SELF_EMPLOYED_INCREASE);
                break;
            case BUSINESS_OWNER:
                rate = rate.add(ScoringConstants.BUSINESS_OWNER_INCREASE);
                log.debug("Business owner: rate has been increased by {}%", ScoringConstants.BUSINESS_OWNER_INCREASE);
                break;
        }
        return rate;
    }

    private BigDecimal recalculationForPosition(BigDecimal rate, Position position) {
        log.debug("Recalculation by position: {}", position);
        switch (position) {
            case MIDDLE_MANAGER:
                rate = rate.subtract(ScoringConstants.MID_MANAGER_DECREASE);
                log.debug("Middle manager: the rate has been reduced by {}%", ScoringConstants.MID_MANAGER_DECREASE);
                break;
            case TOP_MANAGER:
                rate = rate.subtract(ScoringConstants.TOP_MANAGER_DECREASE);
                log.debug("Top manager: the rate has been reduced by {}%", ScoringConstants.TOP_MANAGER_DECREASE);
                break;
        }
        return rate;
    }

    private BigDecimal recalculationForMaritalStatus(BigDecimal rate, MaritalStatus status) {
        log.debug("Recalculation based on marital status: {}", status);
        switch (status) {
            case MARRIED:
                rate = rate.subtract(ScoringConstants.MARRIED_DECREASE);
                log.debug("Married: the rate has been reduced by {}%", ScoringConstants.MARRIED_DECREASE);
                break;
            case DIVORCED:
                rate = rate.add(ScoringConstants.DIVORCED_INCREASE);
                log.debug("Divorced: the rate has been increased by {}%", ScoringConstants.DIVORCED_INCREASE);
                break;
        }
        return rate;
    }

    private BigDecimal recalculationForGenderAndAge(BigDecimal rate, Gender gender, LocalDate birthDate) {
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        log.debug("Recalculation by gender and age: {}, {}", gender, age);
        switch (gender) {
            case FEMALE:
                if (age >= ScoringConstants.FEMALE_AGE_MIN && age <= ScoringConstants.FEMALE_AGE_MAX) {
                    rate = rate.subtract(ScoringConstants.FEMALE_DECREASE);
                    log.debug("Female {} years old: the rate has been reduced by {}%", age, ScoringConstants.FEMALE_DECREASE);
                }
                break;
            case MALE:
                if (age >= ScoringConstants.MALE_AGE_MIN && age <= ScoringConstants.MALE_AGE_MAX) {
                    rate = rate.subtract(ScoringConstants.MALE_DECREASE);
                    log.debug("Male {} years old: the rate has been reduced by {}%", age, ScoringConstants.MALE_DECREASE);
                }
                break;
            case NON_BINARY:
                rate = rate.add(ScoringConstants.NON_BINARY_INCREASE);
                log.debug("Non-binary: the rate has been increased by {}%", ScoringConstants.NON_BINARY_INCREASE);
                break;
        }
        return rate;
    }

    private BigDecimal recalculationForServices(BigDecimal rate, Boolean insurance, Boolean salary) {
        log.debug("Recalculation based on additional services: insurance={}, salary={}",
                insurance, salary);
        if (insurance) {
            rate = rate.subtract(ScoringConstants.INSURANCE_RATE_DECREASE);
            log.debug("Insurance: the rate has been reduced by {}%", ScoringConstants.INSURANCE_RATE_DECREASE);
        }
        if (salary) {
            rate = rate.subtract(ScoringConstants.SALARY_CLIENT_RATE_DECREASE);
            log.debug("Salary client: the rate has been reduced by {}%", ScoringConstants.SALARY_CLIENT_RATE_DECREASE);
        }
        return rate;
    }
}
