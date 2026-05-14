package com.vaultbank.notification.consumer;

import com.vaultbank.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "money.deposited", groupId = "notification-service")
    public void onMoneyDeposited(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("Evento recebido: topic={} offset={}", topic, offset);
        notificationService.processDeposit(payload);
    }

    @KafkaListener(topics = "money.withdrawn", groupId = "notification-service")
    public void onMoneyWithdrawn(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("Evento recebido: topic={} offset={}", topic, offset);
        notificationService.processWithdrawal(payload);
    }

    @KafkaListener(topics = "money.transferred", groupId = "notification-service")
    public void onMoneyTransferred(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("Evento recebido: topic={} offset={}", topic, offset);
        notificationService.processTransfer(payload);
    }
}
