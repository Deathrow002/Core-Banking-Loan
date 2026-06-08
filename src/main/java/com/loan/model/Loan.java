package com.loan.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("loan")
public class Loan {

    @Id
    @Column("id")
    private UUID Id;

    @Column("customer_id")
    private UUID customerId;

    @Column("account_id")
    private UUID accountId;

    @Column("loan_type")
    private String loanType;

    @Column("loan_status")
    private String loanStatus;

    @Column("principal_amount")
    private BigDecimal principalAmount;

    @Column("remaining_balance")
    private BigDecimal remainingBalance;

    @Column("interest_rate")
    private BigDecimal interestRate;

    @Column("term_months")
    private Integer termMonths;

    @Column("monthly_payment")
    private BigDecimal monthlyPayment;

    @Column("start_date")
    private LocalDate startDate;

    @Column("end_date")
    private LocalDate endDate;

    @Column("created_at")
    private LocalDateTime createdAt;

    public Loan(UUID customerId, UUID accountId, String loanType,
                BigDecimal principalAmount, BigDecimal interestRate,
                Integer termMonths, BigDecimal monthlyPayment,
                LocalDate startDate, LocalDate endDate) {
        this.customerId = customerId;
        this.accountId = accountId;
        this.loanType = loanType;
        this.loanStatus = LoanStatus.PENDING.name();
        this.principalAmount = principalAmount;
        this.remainingBalance = principalAmount;
        this.interestRate = interestRate;
        this.termMonths = termMonths;
        this.monthlyPayment = monthlyPayment;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = LocalDateTime.now();
    }
}

