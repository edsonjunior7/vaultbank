package com.vaultbank.account.dto.request;

import com.vaultbank.account.entity.Account;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAccountRequest {

    @NotNull(message = "Tipo de conta e obrigatorio")
    private Account.AccountType accountType;
}
