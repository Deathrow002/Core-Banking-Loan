package com.loan.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.loan.model.Loan;
import com.loan.model.DTO.LoanDTO;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class LoanProcess {

    private static final Logger log = LoggerFactory.getLogger(LoanProcess.class);

    private final LoanService loanService;

    public Mono<Loan> applyLoan(LoanDTO loanDTO, String jwtToken) {
        return loanService.isAccountValid(loanDTO.getAccountId(), jwtToken)
            .flatMap(valid -> {
                if (!valid) {
                    return Mono.error(new IllegalArgumentException("Invalid account: " + loanDTO.getAccountId()));
                }
                return loanService.getAccountDetail(loanDTO.getAccountId(), jwtToken);
            })
            .flatMap(account -> {
                log.info("Account validated for loan application: {}", account.getAccountId());

                BigDecimal monthlyPayment = calculateMonthlyPayment(
                    loanDTO.getPrincipalAmount(),
                    loanDTO.getInterestRate(),
                    loanDTO.getTermMonths()
                );

                LocalDate startDate = loanDTO.getStartDate() != null
                    ? loanDTO.getStartDate()
                    : LocalDate.now();
                LocalDate endDate = startDate.plusMonths(loanDTO.getTermMonths());

                Loan loan = new Loan(
                    loanDTO.getCustomerId(),
                    loanDTO.getAccountId(),
                    loanDTO.getLoanType().name(),
                    loanDTO.getPrincipalAmount(),
                    loanDTO.getInterestRate(),
                    loanDTO.getTermMonths(),
                    monthlyPayment,
                    startDate,
                    endDate
                );

                return loanService.save(loan);
            })
            .doOnSuccess(loan -> log.info("Loan application created: {}", loan.getId()))
            .doOnError(e -> log.error("Loan application failed: {}", e.getMessage()));
    }

    /**
     * Calculates monthly EMI using the standard amortisation formula:
     *   M = P * [r(1+r)^n] / [(1+r)^n - 1]
     * where r = monthly interest rate, n = term in months.
     */
    private BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal annualRate, int termMonths) {
        if (annualRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
        }
        double r = annualRate.doubleValue() / 100.0 / 12.0;
        double n = termMonths;
        double factor = Math.pow(1 + r, n);
        double monthly = principal.doubleValue() * (r * factor) / (factor - 1);
        return BigDecimal.valueOf(monthly).setScale(2, RoundingMode.HALF_UP);
    }
}
