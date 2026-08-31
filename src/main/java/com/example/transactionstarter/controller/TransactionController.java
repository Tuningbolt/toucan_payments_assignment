package com.example.transactionstarter.controller;

import com.example.transactionstarter.dto.StatusUpdateDTO;
import com.example.transactionstarter.dto.TransactionRequestDTO;
import com.example.transactionstarter.dto.TransactionResponseDTO;
import com.example.transactionstarter.service.TransactionService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for transaction operations.
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Task A: Create transaction
     * Accepts a new transaction payload, validates fields via @Valid, and returns 201 Created.
     */
    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(@Valid @RequestBody TransactionRequestDTO dto) {
        log.info("POST /api/transactions - transactionId={}", dto.getTransactionId());
        TransactionResponseDTO response = transactionService.createTransaction(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Task B: Get transaction
     * Retrieves a single transaction by ID. Returns 404 Not Found if missing.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> getTransaction(@PathVariable("id") String id) {
        log.debug("GET /api/transactions/{}", id);
        TransactionResponseDTO response = transactionService.getTransaction(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Task C: Update transaction status
     * Modifies the status of an existing transaction following state-machine rules.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<TransactionResponseDTO> updateTransactionStatus(
            @PathVariable("id") String id,
            @Valid @RequestBody StatusUpdateDTO dto) {
        log.info("PATCH /api/transactions/{}/status - newStatus={}", id, dto.getStatus());
        TransactionResponseDTO response = transactionService.updateTransactionStatus(id, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Task D: Get customer transactions
     * Retrieves all transactions for a given Customer ID. Returns [] if none found.
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<TransactionResponseDTO>> getCustomerTransactions(
            @PathVariable("customerId") String customerId) {
        log.debug("GET /api/transactions/customer/{}", customerId);
        List<TransactionResponseDTO> responses = transactionService.getCustomerTransactions(customerId);
        return ResponseEntity.ok(responses);
    }
}
