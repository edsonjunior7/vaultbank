package com.vaultbank.account.dto.response;

import com.vaultbank.account.entity.LedgerEntry;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LedgerEntryResponse {
    private Long id;
    private BigDecimal amount;
    private LedgerEntry.EntryType entryType;
    private String description;
    private BigDecimal balanceAfter;
    private LocalDateTime createdAt;
}
