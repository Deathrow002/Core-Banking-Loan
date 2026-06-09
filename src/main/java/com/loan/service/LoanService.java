package com.loan.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loan.model.Loan;
import com.loan.model.LoanStatus;
import com.loan.model.DTO.AccountPayload;
import com.loan.repository.LoanRepository;
import com.loan.service.kafka.KafkaProducerService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class LoanService {

    private static final Logger log = LoggerFactory.getLogger(LoanService.class);

    private final LoanRepository loanRepository;
    private final WebClient.Builder webClientBuilder;
    private final KafkaProducerService kafkaProducerService;

    private static final String CHECK_ACCOUNT_URL = "http://ACCOUNT-SERVICE/accounts/validateAccount";
    private static final String GET_ACCOUNT_URL   = "http://ACCOUNT-SERVICE/accounts/getAccount";

    public Mono<Loan> save(Loan loan) {
        return loanRepository.save(loan);
    }

    public Mono<Loan> enquiryByLoanId(UUID loanId) {
        return loanRepository.findById(loanId);
    }

    public Mono<List<Loan>> enquiryByCustomerId(UUID customerId) {
        return loanRepository.findAllByCustomerId(customerId).collectList();
    }

    public Mono<Loan> approveLoan(UUID loanId) {
        return loanRepository.findById(loanId)
            .flatMap(loan -> {
                loan.setLoanStatus(LoanStatus.ACTIVE.name());
                return loanRepository.save(loan);
            });
    }

    public Mono<Loan> closeLoan(UUID loanId) {
        return loanRepository.findById(loanId)
            .flatMap(loan -> {
                loan.setLoanStatus(LoanStatus.CLOSED.name());
                return loanRepository.save(loan);
            });
    }

    public Mono<Loan> makeLoanPayment(UUID loanId, BigDecimal paymentAmount) {
        return loanRepository.findById(loanId)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Loan not found: " + loanId)))
            .flatMap(loan -> {
                if (!LoanStatus.ACTIVE.name().equals(loan.getLoanStatus())) {
                    return Mono.error(new IllegalArgumentException(
                        "Loan is not active. Current status: " + loan.getLoanStatus()));
                }

                BigDecimal amount = paymentAmount.setScale(2, RoundingMode.HALF_UP);
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    return Mono.error(new IllegalArgumentException("Payment amount must be greater than zero"));
                }

                BigDecimal remaining = loan.getRemainingBalance() != null
                    ? loan.getRemainingBalance()
                    : loan.getPrincipalAmount();

                BigDecimal updatedRemaining = remaining.subtract(amount).setScale(2, RoundingMode.HALF_UP);
                if (updatedRemaining.compareTo(BigDecimal.ZERO) <= 0) {
                    loan.setRemainingBalance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
                    loan.setLoanStatus(LoanStatus.CLOSED.name());
                } else {
                    loan.setRemainingBalance(updatedRemaining);
                }

                return loanRepository.save(loan);
            });
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "accountServiceFallback")
    public Mono<Boolean> isAccountValid(UUID accountId, String jwtToken) {
        String url = CHECK_ACCOUNT_URL + "?accNo=" + accountId;
        return webClientBuilder.build().get()
            .uri(url)
            .header("Authorization", "Bearer " + jwtToken)
            .retrieve()
            .bodyToMono(Boolean.class)
            .doOnNext(valid -> log.info("Account {} valid: {}", accountId, valid))
            .onErrorResume(e -> {
                log.warn("Error validating account {}: {}", accountId, e.getMessage());
                return Mono.just(false);
            });
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "getAccountFallback")
    public Mono<AccountPayload> getAccountDetail(UUID accountId, String jwtToken) {
        String url = GET_ACCOUNT_URL + "?accNo=" + accountId;
        return webClientBuilder.build().get()
            .uri(url)
            .header("Authorization", "Bearer " + jwtToken)
            .retrieve()
            .bodyToMono(AccountPayload.class)
            .doOnNext(p -> log.info("Retrieved account details for {}: {}", accountId, p))
            .onErrorResume(e -> {
                log.warn("Error retrieving account details for {}: {}", accountId, e.getMessage());
                return Mono.empty();
            });
    }

    public Mono<Boolean> accountServiceFallback(UUID accountId, String jwtToken, Throwable t) {
        log.error("Fallback: account service unavailable. accountId={}, error={}", accountId, t.getMessage());
        return Mono.just(false);
    }

    public Mono<AccountPayload> getAccountFallback(UUID accountId, String jwtToken, Throwable t) {
        log.error("Fallback: cannot get account. accountId={}, error={}", accountId, t.getMessage());
        return Mono.empty();
    }

    public Mono<Void> publishLoanEvent(String topic, Loan loan, String jwtToken) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        try {
            String payload = objectMapper.writeValueAsString(loan);
            return kafkaProducerService.sendMessageReactive(topic, payload, jwtToken)
                .doOnSuccess(v -> log.info("Published loan event for loan: {}", loan.getId()))
                .doOnError(e -> log.error("Failed to publish loan event for loan: {}: {}", loan.getId(), e.getMessage()));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize Loan: {}", loan.getId(), e);
            return Mono.error(e);
        }
    }
}
