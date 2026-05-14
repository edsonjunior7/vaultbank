package com.vaultbank.transaction.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {

    @NotBlank(message = "Chave de idempotencia obrigatoria")
    private String idempotencyKey;

    @NotBlank(message = "Conta de origem obrigatoria")
    private String sourceAccount;

    @NotBlank(message = "Conta de destino obrigatoria")
    private String destinationAccount;

    @NotNull(message = "Valor obrigatorio")
    @DecimalMin(value = "0.01", message = "Valor minimo e R$ 0,01")
    @DecimalMax(value = "50000.00", message = "Valor maximo por transferencia e R$ 50.000")
    private BigDecimal amount;

    @Size(max = 255)
    private String description;
}
