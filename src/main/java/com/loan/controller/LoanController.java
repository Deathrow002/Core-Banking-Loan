package com.loan.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.loan.model.Loan;
import com.loan.model.DTO.LoanDTO;
import com.loan.model.DTO.LoanPaymentDTO;
import com.loan.service.LoanProcess;
import com.loan.service.LoanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/loans")
public class LoanController {

    private final LoanService loanService;
    private final LoanProcess loanProcess;

    @PostMapping("/apply")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public Mono<ResponseEntity<Loan>> apply(@Valid @RequestBody LoanDTO loanDTO,
                                            @RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        return loanProcess.applyLoan(loanDTO, jwtToken)
            .map(loan -> ResponseEntity.status(HttpStatus.CREATED).body(loan));
    }

    @PostMapping("/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public Mono<ResponseEntity<Loan>> approve(@RequestParam UUID loanId) {
        return loanService.approveLoan(loanId)
            .map(loan -> ResponseEntity.status(HttpStatus.OK).body(loan));
    }

    @PostMapping("/close")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public Mono<ResponseEntity<Loan>> close(@RequestParam UUID loanId) {
        return loanService.closeLoan(loanId)
            .map(loan -> ResponseEntity.status(HttpStatus.OK).body(loan));
    }

    @PostMapping("/pay")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public Mono<ResponseEntity<Loan>> pay(@Valid @RequestBody LoanPaymentDTO paymentDTO) {
        return loanProcess.processLoanPayment(paymentDTO)
            .map(loan -> ResponseEntity.status(HttpStatus.OK).body(loan));
    }

    @GetMapping("/getById")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public Mono<ResponseEntity<Loan>> getById(@RequestParam UUID loanId) {
        return loanService.findById(loanId)
            .map(loan -> ResponseEntity.status(HttpStatus.OK).body(loan));
    }

    @GetMapping("/getByCustomer")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public Mono<ResponseEntity<?>> getByCustomer(@RequestParam UUID customerId) {
        return loanService.getLoansByCustomer(customerId)
            .map(list -> ResponseEntity.status(HttpStatus.OK).body(list));
    }
}
