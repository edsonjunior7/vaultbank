package com.vaultbank.auth.service;

import com.vaultbank.auth.dto.request.LoginRequest;
import com.vaultbank.auth.dto.request.RefreshTokenRequest;
import com.vaultbank.auth.dto.request.RegisterRequest;
import com.vaultbank.auth.dto.response.AuthResponse;
import com.vaultbank.auth.entity.LoginAttempt;
import com.vaultbank.auth.entity.RefreshToken;
import com.vaultbank.auth.entity.Role;
import com.vaultbank.auth.entity.User;
import com.vaultbank.auth.exception.AccountLockedException;
import com.vaultbank.auth.exception.BusinessException;
import com.vaultbank.auth.repository.LoginAttemptRepository;
import com.vaultbank.auth.repository.RefreshTokenRepository;
import com.vaultbank.auth.repository.RoleRepository;
import com.vaultbank.auth.repository.UserRepository;
import com.vaultbank.auth.security.CryptoService;
import com.vaultbank.auth.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CryptoService cryptoService;
    private final AuthenticationManager authenticationManager;

    @Value("${security.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${security.lock-duration-minutes:15}")
    private int lockDurationMinutes;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email ja cadastrado");
        }

        // Verifica CPF pelo hash (sem descriptografar outros registros)
        String documentHash = cryptoService.hash(request.getDocumentNumber());
        if (userRepository.existsByDocumentHash(documentHash)) {
            throw new BusinessException("CPF ja cadastrado");
        }

        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new BusinessException("Role CUSTOMER nao encontrada"));

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                // CPF criptografado com AES-256-GCM
                .documentNumber(cryptoService.encrypt(request.getDocumentNumber()))
                // Hash para busca eficiente
                .documentHash(documentHash)
                // Telefone criptografado
                .phone(request.getPhone() != null ? cryptoService.encrypt(request.getPhone()) : null)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(customerRole))
                .build();

        userRepository.save(user);
        log.info("Usuario registrado: {} (CPF e telefone criptografados)", user.getEmail());

        return buildAuthResponse(user, httpRequest);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String ip = extractIp(httpRequest);
        checkAccountLock(request.getEmail());

        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (LockedException e) {
            recordAttempt(request.getEmail(), ip, httpRequest, false);
            throw new AccountLockedException("Conta bloqueada. Tente novamente em " + lockDurationMinutes + " minutos.");
        } catch (BadCredentialsException e) {
            recordAttempt(request.getEmail(), ip, httpRequest, false);
            long remaining = maxLoginAttempts - countRecentFailures(request.getEmail());
            if (remaining <= 0) {
                throw new AccountLockedException("Conta bloqueada por excesso de tentativas.");
            }
            throw new BusinessException("Credenciais invalidas. Tentativas restantes: " + remaining);
        }

        recordAttempt(request.getEmail(), ip, httpRequest, true);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Usuario nao encontrado"));

        refreshTokenRepository.revokeAllUserTokens(user.getId());
        return buildAuthResponse(user, httpRequest);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException("Refresh token invalido"));

        if (!stored.isValid()) {
            throw new BusinessException("Refresh token expirado ou revogado");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
        return buildAuthResponse(stored.getUser(), httpRequest);
    }

    @Transactional
    public void logout(String accessToken, String refreshTokenStr) {
        jwtService.blacklist(accessToken);
        refreshTokenRepository.findByToken(refreshTokenStr).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    private AuthResponse buildAuthResponse(User user, HttpServletRequest httpRequest) {
        List<String> roles = user.getRoles().stream().map(Role::getName).toList();
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), roles);
        String refreshTokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000))
                .ipAddress(extractIp(httpRequest))
                .userAgent(httpRequest.getHeader("User-Agent"))
                .build();

        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(refreshExpiration / 1000)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .roles(roles)
                        .build())
                .build();
    }

    private void checkAccountLock(String email) {
        if (countRecentFailures(email) >= maxLoginAttempts) {
            throw new AccountLockedException("Conta temporariamente bloqueada. Aguarde " + lockDurationMinutes + " minutos.");
        }
    }

    private long countRecentFailures(String email) {
        return loginAttemptRepository.countFailedAttemptsSince(email,
                LocalDateTime.now().minusMinutes(lockDurationMinutes));
    }

    private void recordAttempt(String email, String ip, HttpServletRequest request, boolean success) {
        loginAttemptRepository.save(LoginAttempt.builder()
                .email(email)
                .ipAddress(ip)
                .success(success)
                .userAgent(request.getHeader("User-Agent"))
                .build());
    }

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
