package com.loan.service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "account-events", groupId = "loan-service-group")
    public void consumeAccountEvent(String message) {
        // Process account-related events that affect loans
    }

    @KafkaListener(topics = "transaction-events", groupId = "loan-service-group")
    public void consumeTransactionEvent(String message) {
        // Process transaction events (e.g., repayments) that affect loan balance
    }
}
