package com.vaultbank.audit.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaultbank.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventConsumer {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "money.deposited", groupId = "audit-service")
    public void onDeposit(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            auditService.recordTransaction(
                    node.get("transactionId").asLong(),
                    node.get("userId").asLong(),
                    "DEPOSIT",
                    "Deposito de R$ " + node.get("amount").asText() +
                    " na conta " + node.get("accountNumber").asText());
        } catch (Exception e) {
            log.error("Erro ao auditar deposito: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "money.withdrawn", groupId = "audit-service")
    public void onWithdrawal(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            auditService.recordTransaction(
                    node.get("transactionId").asLong(),
                    node.get("userId").asLong(),
                    "WITHDRAWAL",
                    "Saque de R$ " + node.get("amount").asText() +
                    " da conta " + node.get("accountNumber").asText());
        } catch (Exception e) {
            log.error("Erro ao auditar saque: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "money.transferred", groupId = "audit-service")
    public void onTransfer(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            auditService.recordTransaction(
                    node.get("transactionId").asLong(),
                    node.get("userId").asLong(),
                    "TRANSFER",
                    "Transferencia de R$ " + node.get("amount").asText() +
                    " de " + node.get("sourceAccount").asText() +
                    " para " + node.get("destinationAccount").asText());
        } catch (Exception e) {
            log.error("Erro ao auditar transferencia: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "fraud.detected", groupId = "audit-service")
    public void onFraud(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            auditService.recordFraud(
                    node.get("transactionId").asLong(),
                    node.get("userId").asLong(),
                    "Score: " + node.get("riskScore").asText() +
                    " | Razoes: " + node.get("reasons").toString());
        } catch (Exception e) {
            log.error("Erro ao auditar fraude: {}", e.getMessage());
        }
    }
}
