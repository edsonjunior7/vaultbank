package com.vaultbank.transaction.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositWithdrawRequest {

    @NotBlank(message = "Chave de idempotencia obrigatoria")
    private String idempotencyKey;

    @NotBlank(message = "Numero da conta obrigatorio")
    private String accountNumber;

    @NotNull(message = "Valor obrigatorio")
    @DecimalMin(value = "0.01", message = "Valor minimo e R$ 0,01")
    private BigDecimal amount;

    @Size(max = 255)
    private String description;
}
