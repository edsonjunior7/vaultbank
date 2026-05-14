package com.vaultbank.account.dto.response;

import com.vaultbank.account.entity.Account;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AccountResponse {
    private Long id;
    private String accountNumber;
    private Account.AccountType accountType;
    private Account.AccountStatus status;
    private String currency;
    private BigDecimal balance;
    private LocalDateTime createdAt;
}
