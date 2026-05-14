package com.vaultbank.account.dto.request;

import com.vaultbank.account.entity.LedgerEntry;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LedgerEntryRequest {

    @NotBlank(message = "Numero da conta e obrigatorio")
    private String accountNumber;

    @NotNull(message = "Valor e obrigatorio")
    private BigDecimal amount;

    @NotNull(message = "Tipo de entrada e obrigatorio")
    private LedgerEntry.EntryType entryType;

    private String description;

    private String referenceId;
}
