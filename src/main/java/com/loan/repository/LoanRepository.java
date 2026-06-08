package com.loan.repository;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.loan.model.Loan;

import reactor.core.publisher.Flux;

@Repository
public interface LoanRepository extends ReactiveCrudRepository<Loan, UUID> {
    Flux<Loan> findAllByCustomerId(UUID customerId);
    Flux<Loan> findAllByAccountId(UUID accountId);
}
