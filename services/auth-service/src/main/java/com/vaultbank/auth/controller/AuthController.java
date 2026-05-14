package com.vaultbank.auth.controller;

import com.vaultbank.auth.dto.request.LoginRequest;
import com.vaultbank.auth.dto.request.RefreshTokenRequest;
import com.vaultbank.auth.dto.request.RegisterRequest;
import com.vaultbank.auth.dto.response.ApiResponse;
import com.vaultbank.auth.dto.response.AuthResponse;
import com.vaultbank.auth.security.JwtService;
import com.vaultbank.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticacao e autorizacao")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    @Operation(summary = "Criar nova conta")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthResponse auth = authService.register(request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Conta criada com sucesso", auth));
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthResponse auth = authService.login(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("Login realizado com sucesso", auth));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthResponse auth = authService.refresh(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("Token renovado", auth));
    }

    @PostMapping("/logout")
    @Operation(summary = "Encerrar sessao", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest httpRequest,
            @RequestParam(required = false) String refreshToken
    ) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            authService.logout(accessToken, refreshToken != null ? refreshToken : "");
        }
        return ResponseEntity.ok(ApiResponse.ok("Logout realizado", null));
    }

    @GetMapping("/me")
    @Operation(summary = "Dados do usuario autenticado", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<MeResponse>> me(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .toList();

        MeResponse response = MeResponse.builder()
                .email(userDetails.getUsername())
                .roles(roles)
                .active(userDetails.isEnabled() && userDetails.isAccountNonLocked())
                .build();

        return ResponseEntity.ok(ApiResponse.ok("Usuario autenticado", response));
    }

    @Builder
    @Data
    public static class MeResponse {
        private String email;
        private List<String> roles;
        private boolean active;
    }
}
