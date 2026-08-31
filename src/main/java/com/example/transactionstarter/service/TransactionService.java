package com.example.transactionstarter.service;

import com.example.transactionstarter.dto.StatusUpdateDTO;
import com.example.transactionstarter.dto.TransactionRequestDTO;
import com.example.transactionstarter.dto.TransactionResponseDTO;

import java.util.List;

public interface TransactionService {

    /** Task A: Create transaction, validate, enforce PENDING status, and reject duplicate IDs. */
    TransactionResponseDTO createTransaction(TransactionRequestDTO dto);

    /** Task B: Get transaction by ID or throw TransactionNotFoundException if missing. */
    TransactionResponseDTO getTransaction(String transactionId);

    /** Task C: Update status while enforcing state-machine rules (terminal states are locked). */
    TransactionResponseDTO updateTransactionStatus(String transactionId, StatusUpdateDTO dto);

    /** Task D: Retrieve all transactions belonging to a specific Customer ID. */
    List<TransactionResponseDTO> getCustomerTransactions(String customerId);
}
