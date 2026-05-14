package com.vaultbank.auth.service;

import com.vaultbank.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenCleanupService {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Limpa tokens expirados todo dia à meia-noite.
     * Em produção isso poderia ser uma lambda ou job separado.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Iniciando limpeza de refresh tokens expirados...");
        refreshTokenRepository.deleteExpiredTokens();
        log.info("Limpeza concluída");
    }
}
