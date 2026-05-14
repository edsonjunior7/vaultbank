package com.vaultbank.fraud.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaultbank.fraud.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionFraudConsumer {

    private final FraudDetectionService fraudDetectionService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "money.deposited", groupId = "fraud-service")
    public void onDeposit(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            fraudDetectionService.analyze(
                    node.get("transactionId").asLong(),
                    node.get("userId").asLong(),
                    node.get("accountNumber").asText(),
                    new BigDecimal(node.get("amount").asText()),
                    "unknown"
            );
        } catch (Exception e) {
            log.error("Erro ao analisar deposito: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "money.withdrawn", groupId = "fraud-service")
    public void onWithdrawal(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            fraudDetectionService.analyze(
                    node.get("transactionId").asLong(),
                    node.get("userId").asLong(),
                    node.get("accountNumber").asText(),
                    new BigDecimal(node.get("amount").asText()),
                    "unknown"
            );
        } catch (Exception e) {
            log.error("Erro ao analisar saque: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "money.transferred", groupId = "fraud-service")
    public void onTransfer(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            fraudDetectionService.analyze(
                    node.get("transactionId").asLong(),
                    node.get("userId").asLong(),
                    node.get("sourceAccount").asText(),
                    new BigDecimal(node.get("amount").asText()),
                    "unknown"
            );
        } catch (Exception e) {
            log.error("Erro ao analisar transferencia: {}", e.getMessage());
        }
    }
}
