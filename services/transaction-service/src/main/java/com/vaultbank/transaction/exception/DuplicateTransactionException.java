package com.vaultbank.transaction.exception;

import com.vaultbank.transaction.dto.response.TransactionResponse;
import lombok.Getter;

@Getter
public class DuplicateTransactionException extends RuntimeException {
    private final TransactionResponse existingTransaction;

    public DuplicateTransactionException(String message, TransactionResponse existingTransaction) {
        super(message);
        this.existingTransaction = existingTransaction;
    }
}
