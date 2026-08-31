package com.example.transactionstarter.exception;

import com.example.transactionstarter.model.TransactionStatus;

public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(TransactionStatus currentStatus, TransactionStatus requestedStatus) {
        super(String.format(
                "Invalid state transition: cannot move from %s to %s. %s is a terminal state and cannot be changed.",
                currentStatus, requestedStatus, currentStatus));
    }
}
