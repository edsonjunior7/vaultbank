package com.vaultbank.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaultbank.notification.entity.Notification;
import com.vaultbank.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void processDeposit(String payload) {
        try {
            var node = objectMapper.readTree(payload);
            Long userId = node.get("userId").asLong();
            BigDecimal amount = new BigDecimal(node.get("amount").asText());
            String account = node.get("accountNumber").asText();
            Long txId = node.get("transactionId").asLong();

            Notification notification = Notification.builder()
                    .userId(userId)
                    .type(Notification.NotificationType.DEPOSIT)
                    .title("Deposito recebido")
                    .message(String.format("Voce recebeu um deposito de R$ %.2f na conta %s", amount, account))
                    .referenceId(txId.toString())
                    .status(Notification.NotificationStatus.SENT)
                    .sentAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);
            log.info("Notificacao de deposito criada para usuario {}: R$ {}", userId, amount);

        } catch (Exception e) {
            log.error("Erro ao processar evento de deposito: {}", e.getMessage());
        }
    }

    @Transactional
    public void processWithdrawal(String payload) {
        try {
            var node = objectMapper.readTree(payload);
            Long userId = node.get("userId").asLong();
            BigDecimal amount = new BigDecimal(node.get("amount").asText());
            String account = node.get("accountNumber").asText();
            Long txId = node.get("transactionId").asLong();

            Notification notification = Notification.builder()
                    .userId(userId)
                    .type(Notification.NotificationType.WITHDRAWAL)
                    .title("Saque realizado")
                    .message(String.format("Saque de R$ %.2f realizado na conta %s", amount, account))
                    .referenceId(txId.toString())
                    .status(Notification.NotificationStatus.SENT)
                    .sentAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);
            log.info("Notificacao de saque criada para usuario {}: R$ {}", userId, amount);

        } catch (Exception e) {
            log.error("Erro ao processar evento de saque: {}", e.getMessage());
        }
    }

    @Transactional
    public void processTransfer(String payload) {
        try {
            var node = objectMapper.readTree(payload);
            Long userId = node.get("userId").asLong();
            BigDecimal amount = new BigDecimal(node.get("amount").asText());
            String source = node.get("sourceAccount").asText();
            String destination = node.get("destinationAccount").asText();
            Long txId = node.get("transactionId").asLong();

            // Notificacao para quem enviou
            Notification sent = Notification.builder()
                    .userId(userId)
                    .type(Notification.NotificationType.TRANSFER_OUT)
                    .title("Transferencia enviada")
                    .message(String.format("Transferencia de R$ %.2f enviada da conta %s para %s",
                            amount, source, destination))
                    .referenceId(txId.toString())
                    .status(Notification.NotificationStatus.SENT)
                    .sentAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(sent);
            log.info("Notificacao de transferencia criada para usuario {}: R$ {}", userId, amount);

        } catch (Exception e) {
            log.error("Erro ao processar evento de transferencia: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<Notification> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
}
