package ru.gudoshnikova.calculator.util;

import java.math.BigDecimal;

public final class ScoringConstants {
    private ScoringConstants() {
    }

    public static final int MIN_AGE = 20;
    public static final int MAX_AGE = 65;
    public static final int FEMALE_AGE_MIN = 32;
    public static final int FEMALE_AGE_MAX = 60;
    public static final int MALE_AGE_MIN = 30;
    public static final int MALE_AGE_MAX = 55;

    public static final int MAX_LOAN_TO_SALARY_RATIO = 24;
    public static final int MIN_TOTAL_EXPERIENCE = 18;
    public static final int MIN_CURRENT_EXPERIENCE = 3;

    public static final BigDecimal SELF_EMPLOYED_INCREASE = BigDecimal.valueOf(2.0);
    public static final BigDecimal BUSINESS_OWNER_INCREASE = BigDecimal.valueOf(1.0);
    public static final BigDecimal MID_MANAGER_DECREASE = BigDecimal.valueOf(2.0);
    public static final BigDecimal TOP_MANAGER_DECREASE = BigDecimal.valueOf(3.0);
    public static final BigDecimal MARRIED_DECREASE = BigDecimal.valueOf(3.0);
    public static final BigDecimal DIVORCED_INCREASE = BigDecimal.valueOf(1.0);
    public static final BigDecimal FEMALE_DECREASE = BigDecimal.valueOf(3.0);
    public static final BigDecimal MALE_DECREASE = BigDecimal.valueOf(3.0);
    public static final BigDecimal NON_BINARY_INCREASE = BigDecimal.valueOf(7.0);

    public static final BigDecimal INSURANCE_RATE_DECREASE = BigDecimal.valueOf(3.0);
    public static final BigDecimal SALARY_CLIENT_RATE_DECREASE = BigDecimal.valueOf(1.0);
}
