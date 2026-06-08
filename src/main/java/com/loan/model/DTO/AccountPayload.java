package com.loan.model.DTO;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountPayload {
    @JsonProperty("accountId")
    private UUID accountId;

    @JsonProperty("customerId")
    private UUID customerId;

    @JsonProperty("balance")
    private BigDecimal balance;

    @JsonProperty("currency")
    private String currency;

    public AccountPayload(UUID accountId, BigDecimal balance, String currency) {
        this.accountId = accountId;
        this.balance = balance;
        this.currency = currency;
    }
}
