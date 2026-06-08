package com.loan.model.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.loan.model.LoanType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanDTO {
    private UUID loanId;
    private UUID customerId;
    private UUID accountId;
    private LoanType loanType;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer termMonths;
    private LocalDate startDate;

    public LoanDTO(UUID customerId, UUID accountId, LoanType loanType,
                   BigDecimal principalAmount, BigDecimal interestRate,
                   Integer termMonths) {
        this.customerId = customerId;
        this.accountId = accountId;
        this.loanType = loanType;
        this.principalAmount = principalAmount;
        this.interestRate = interestRate;
        this.termMonths = termMonths;
        this.startDate = LocalDate.now();
    }
}
