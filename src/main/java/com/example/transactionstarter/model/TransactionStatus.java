package com.example.transactionstarter.model;

public enum TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REJECTED;

    public boolean isTerminal() {
        return this != PENDING;
    }
}
