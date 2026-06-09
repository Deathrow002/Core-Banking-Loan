package com.loan.model;

public enum LoanType {

    /** Standard amortising EMI: M = P·r(1+r)^n / [(1+r)^n − 1] */
    PERSONAL(CalculationType.AMORTISING),

    /** Compound interest: A = P(1 + r/n)^(n·t), monthly payment = A / termMonths */
    MORTGAGE(CalculationType.COMPOUND),

    /** Annual simple interest: total = P(1 + r·t), monthly payment = total / termMonths */
    AUTO(CalculationType.ANNUAL_SIMPLE),

    /** Standard amortising EMI */
    BUSINESS(CalculationType.AMORTISING),

    /** Standard amortising EMI */
    EDUCATION(CalculationType.AMORTISING);

    private final CalculationType calculationType;

    LoanType(CalculationType calculationType) {
        this.calculationType = calculationType;
    }

    public CalculationType getCalculationType() {
        return calculationType;
    }

    public enum CalculationType {
        AMORTISING,
        COMPOUND,
        ANNUAL_SIMPLE,
        ADD_ON
    }
}
