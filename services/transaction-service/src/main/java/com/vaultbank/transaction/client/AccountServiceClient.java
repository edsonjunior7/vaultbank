package com.vaultbank.transaction.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
public class AccountServiceClient {

    private final RestTemplate restTemplate;
    private final String accountServiceUrl;

    public AccountServiceClient(
            RestTemplate restTemplate,
            @Value("${services.account-service-url}") String accountServiceUrl
    ) {
        this.restTemplate = restTemplate;
        this.accountServiceUrl = accountServiceUrl;
    }

    public void addLedgerEntry(String accountNumber, BigDecimal amount,
                                String entryType, String description, String referenceId) {
        String url = accountServiceUrl + "/accounts/internal/ledger";

        Map<String, Object> body = Map.of(
                "accountNumber", accountNumber,
                "amount", amount,
                "entryType", entryType,
                "description", description != null ? description : "",
                "referenceId", referenceId
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, request, Object.class);
            log.info("Ledger entry registrada: conta={} valor={} tipo={}", accountNumber, amount, entryType);
        } catch (Exception e) {
            log.error("Falha ao registrar ledger entry: {}", e.getMessage());
            throw new RuntimeException("Falha ao comunicar com account-service: " + e.getMessage());
        }
    }
}
