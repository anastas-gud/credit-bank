package ru.gudoshnikova.calculator.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gudoshnikova.calculator.config.CalculatorConfig;
import ru.gudoshnikova.calculator.dto.CreditDto;
import ru.gudoshnikova.calculator.dto.LoanOfferDto;
import ru.gudoshnikova.calculator.dto.LoanStatementRequestDto;
import ru.gudoshnikova.calculator.dto.PaymentScheduleElementDto;
import ru.gudoshnikova.calculator.dto.ScoringDataDto;
import ru.gudoshnikova.calculator.service.CalculatorService;
import ru.gudoshnikova.calculator.service.PrescoringService;
import ru.gudoshnikova.calculator.service.ScoringService;
import ru.gudoshnikova.calculator.util.ScoringConstants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculatorServiceImpl implements CalculatorService {

    private final PrescoringService prescoringService;
    private final ScoringService scoringService;
    private final CalculatorConfig config;

    @Override
    public List<LoanOfferDto> calculateLoanOffers(LoanStatementRequestDto request) {
        log.info("Calculation of loan offers on request: {}", request);

        log.info("The beginning of prescoring");
        prescoringService.prescoring(request);
        log.info("Prescoring is completed");


        List<LoanOfferDto> offers = new ArrayList<>();

        boolean[] insuranceOptions = {false, true};
        boolean[] salaryOptions = {false, true};

        for (Boolean insurance : insuranceOptions) {
            for (Boolean salary : salaryOptions) {
                LoanOfferDto offer = createLoanOffer(request, insurance, salary);
                offers.add(offer);

                log.debug("An offer has been created: rate={}%, totalAmount={}, monthlyPayment={}",
                        offer.getRate(), offer.getTotalAmount(), offer.getMonthlyPayment());
            }
        }

        offers.sort(Comparator.comparing(LoanOfferDto::getRate).reversed());
        log.info("Full list of offers: {}", offers);
        return offers;
    }

    @Override
    public CreditDto calculateCredit(ScoringDataDto scoringData) {
        log.info("The beginning of the calculation of the full loan according to the data: {}", scoringData);

        BigDecimal finalRate;
        log.info("The beginning of scoring");
        finalRate = scoringService.score(scoringData);
        log.info("Scoring is completed. Final rate: {}%", finalRate);

        BigDecimal creditAmount = scoringData.getAmount();

        if (scoringData.getIsInsuranceEnabled()) {
            BigDecimal insuranceCost = creditAmount
                    .multiply(config.getInsuranceCostPercent())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            creditAmount = creditAmount.add(insuranceCost);
            log.debug("Insurance included: insurance cost = {}, loan amount increased to {}",
                    insuranceCost, creditAmount);
        }

        BigDecimal monthlyPayment = calculateMonthlyPayment(creditAmount,
                finalRate, scoringData.getTerm());
        log.info("Monthly payment: {}", monthlyPayment);

        List<PaymentScheduleElementDto> paymentSchedule = calculatePaymentSchedule(creditAmount,
                finalRate, scoringData.getTerm(), monthlyPayment);
        log.info("The payment schedule is calculated ({} payments)", paymentSchedule.size());

        BigDecimal psk = calculatePsk(scoringData.getAmount(),
                creditAmount, finalRate, scoringData.getTerm());
        log.info("PSK: {}%", psk);

        return CreditDto.builder()
                .amount(creditAmount)
                .term(scoringData.getTerm())
                .monthlyPayment(monthlyPayment)
                .rate(finalRate)
                .psk(psk)
                .isInsuranceEnabled(scoringData.getIsInsuranceEnabled())
                .isSalaryClient(scoringData.getIsSalaryClient())
                .paymentSchedule(paymentSchedule)
                .build();
    }

    private LoanOfferDto createLoanOffer(LoanStatementRequestDto request,
                                         Boolean insurance, Boolean salary) {
        log.debug("Creating an offer with parameters: insurance={}, salary={}",
                insurance, salary);

        BigDecimal rate = config.getBaseRate();
        BigDecimal totalAmount = request.getAmount();

        if (insurance) {
            rate = rate.subtract(ScoringConstants.INSURANCE_RATE_DECREASE);

            BigDecimal insuranceCost = request.getAmount()
                    .multiply(config.getInsuranceCostPercent())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            totalAmount = totalAmount.add(insuranceCost);

            log.debug("Insurance: the rate has been reduced by {}%, the cost of insurance: {}",
                    ScoringConstants.INSURANCE_RATE_DECREASE, insuranceCost);
        }

        if (salary) {
            rate = rate.subtract(ScoringConstants.SALARY_CLIENT_RATE_DECREASE);

            log.debug("Salary client: the rate has been reduced by {}%",
                    ScoringConstants.SALARY_CLIENT_RATE_DECREASE);
        }

        BigDecimal monthlyPayment = calculateMonthlyPayment(totalAmount, rate, request.getTerm());

        log.debug("The monthly payment is calculated: {}", monthlyPayment);

        return LoanOfferDto.builder()
                .statementId(UUID.randomUUID())
                .requestedAmount(request.getAmount())
                .totalAmount(totalAmount)
                .term(request.getTerm())
                .monthlyPayment(monthlyPayment)
                .rate(rate)
                .isInsuranceEnabled(insurance)
                .isSalaryClient(salary)
                .build();
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal totalAmount, BigDecimal rate, Integer term) {
        log.debug("Calculation of the monthly payment: amount={}, annualRate={}%, term={}",
                totalAmount, rate, term);

        BigDecimal monthlyRate = rate.
                divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP).
                divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        log.debug("Monthly rate: {}", monthlyRate);

        BigDecimal ratePower = BigDecimal.ONE.add(monthlyRate).pow(term);
        BigDecimal numerator = monthlyRate.multiply(ratePower);
        BigDecimal denominator = ratePower.subtract(BigDecimal.ONE);

        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("The denominator is 0, a simplified calculation is used.");
            return totalAmount.divide(BigDecimal.valueOf(term), 10, RoundingMode.HALF_UP);
        }

        BigDecimal annuityCoefficient = numerator.divide(denominator, 10, RoundingMode.HALF_UP);

        return totalAmount.multiply(annuityCoefficient).setScale(2, RoundingMode.HALF_UP);
    }

    private List<PaymentScheduleElementDto> calculatePaymentSchedule(BigDecimal amount,
                                                                     BigDecimal rate, int term,
                                                                     BigDecimal monthlyPayment) {
        log.debug("Calculation of the payment schedule: amount={}, rate={}%, term={}, monthlyPayment={}",
                amount, rate, term, monthlyPayment);

        List<PaymentScheduleElementDto> schedule = new ArrayList<>();
        BigDecimal remainingDebt = amount;
        LocalDate date = LocalDate.now().plusMonths(1);
        BigDecimal monthlyRate = rate.
                divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP).
                divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        log.debug("Monthly rate for the schedule: {}", monthlyRate);

        for (int i = 1; i <= term; i++) {
            log.trace("Payment calculation №{}", i);

            BigDecimal interestPayment = remainingDebt.multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal debtPayment = monthlyPayment.subtract(interestPayment);

            if (i == term) {
                debtPayment = remainingDebt;
                monthlyPayment = debtPayment.add(interestPayment);
            }

            remainingDebt = remainingDebt.subtract(debtPayment)
                    .setScale(2, RoundingMode.HALF_UP);

            PaymentScheduleElementDto element = PaymentScheduleElementDto.builder()
                    .number(i)
                    .date(date)
                    .totalPayment(monthlyPayment)
                    .interestPayment(interestPayment)
                    .debtPayment(debtPayment)
                    .remainingDebt(remainingDebt)
                    .build();
            schedule.add(element);

            log.trace("Payment {}: date={}, monthlyPayment={}, interestPayment={}, " +
                            "debtPayment={}, remainingDebt={}",
                    i, date, monthlyPayment, interestPayment, debtPayment, remainingDebt);

            date = date.plusMonths(1);
        }
        return schedule;
    }

    private BigDecimal calculatePsk(BigDecimal amount,
                                    BigDecimal totalAmount,
                                    BigDecimal rate,
                                    Integer term) {
        log.debug("Calculation of the PSK");

        BigDecimal termInYears = BigDecimal.valueOf(term)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        log.debug("Term in years {}", termInYears);

        BigDecimal percentages = amount
                .multiply(rate)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .multiply(termInYears);

        log.debug("Interest to the bank: {}", percentages);

        BigDecimal addExpenses = totalAmount.subtract(amount);

        log.debug("Additional expenses: {}", addExpenses);

        BigDecimal pskRubles = amount.add(percentages).add(addExpenses);
        log.debug("PSK in rubles: {}", pskRubles);

        BigDecimal pskPercentages = pskRubles
                .divide(amount, 10, RoundingMode.HALF_UP)
                .subtract(BigDecimal.valueOf(1))
                .divide(termInYears, 10, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        log.info("PSK: {}", pskPercentages);

        return pskPercentages;
    }
}
