package com.vaultbank.transaction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishDepositEvent(Long transactionId, Long userId,
                                     String accountNumber, BigDecimal amount, String description) {
        publish("money.deposited", Map.of(
                "transactionId", transactionId,
                "userId", userId,
                "accountNumber", accountNumber,
                "amount", amount,
                "description", description != null ? description : "",
                "occurredAt", LocalDateTime.now().toString()
        ));
    }

    public void publishWithdrawalEvent(Long transactionId, Long userId,
                                        String accountNumber, BigDecimal amount, String description) {
        publish("money.withdrawn", Map.of(
                "transactionId", transactionId,
                "userId", userId,
                "accountNumber", accountNumber,
                "amount", amount,
                "description", description != null ? description : "",
                "occurredAt", LocalDateTime.now().toString()
        ));
    }

    public void publishTransferEvent(Long transactionId, Long userId,
                                      String sourceAccount, String destinationAccount,
                                      BigDecimal amount, String description) {
        publish("money.transferred", Map.of(
                "transactionId", transactionId,
                "userId", userId,
                "sourceAccount", sourceAccount,
                "destinationAccount", destinationAccount,
                "amount", amount,
                "description", description != null ? description : "",
                "occurredAt", LocalDateTime.now().toString()
        ));
    }

    private void publish(String topic, Map<String, Object> payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, json);
            log.info("Evento publicado: topic={} payload={}", topic, json);
        } catch (Exception e) {
            // Kafka falhou — nao quebra a transacao principal
            log.error("Falha ao publicar evento no Kafka: topic={} erro={}", topic, e.getMessage());
        }
    }
}
