package com.example.transactionstarter.dto;

import com.example.transactionstarter.model.TransactionStatus;
import jakarta.validation.constraints.NotNull;

public class StatusUpdateDTO {

    @NotNull(message = "Status is required")
    private TransactionStatus status;

    public StatusUpdateDTO() {
    }

    public StatusUpdateDTO(TransactionStatus status) {
        this.status = status;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
}
