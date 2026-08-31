package com.example.transactionstarter.service;

import com.example.transactionstarter.dto.StatusUpdateDTO;
import com.example.transactionstarter.dto.TransactionRequestDTO;
import com.example.transactionstarter.dto.TransactionResponseDTO;
import com.example.transactionstarter.exception.DuplicateTransactionException;
import com.example.transactionstarter.exception.InvalidStateTransitionException;
import com.example.transactionstarter.exception.TransactionNotFoundException;
import com.example.transactionstarter.model.Transaction;
import com.example.transactionstarter.model.TransactionStatus;
import com.example.transactionstarter.repository.TransactionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Task A: Create transaction
     * Checks for duplicate IDs, forces status to PENDING, and persists entity.
     */
    @Override
    @Transactional
    public TransactionResponseDTO createTransaction(TransactionRequestDTO dto) {
        log.info("Creating transaction with id: {}", dto.getTransactionId());

        if (transactionRepository.existsById(dto.getTransactionId())) {
            throw new DuplicateTransactionException(dto.getTransactionId());
        }

        Transaction entity = mapToEntity(dto);
        entity.setStatus(TransactionStatus.PENDING); // Always force PENDING status on creation

        Transaction saved = transactionRepository.saveAndFlush(entity);
        log.info("Transaction created successfully: {}", saved.getTransactionId());

        return mapToResponseDTO(saved);
    }

    /**
     * Task B: Get transaction by ID
     * Throws TransactionNotFoundException if missing.
     */
    @Override
    @Transactional(readOnly = true)
    public TransactionResponseDTO getTransaction(String transactionId) {
        log.debug("Fetching transaction: {}", transactionId);

        Transaction entity = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        return mapToResponseDTO(entity);
    }

    /**
     * Task C: Update transaction status
     * Enforces state machine rules: terminal states (COMPLETED, FAILED, REJECTED) cannot transition.
     */
    @Override
    @Transactional
    public TransactionResponseDTO updateTransactionStatus(String transactionId, StatusUpdateDTO dto) {
        log.info("Updating status of transaction {} to {}", transactionId, dto.getStatus());

        Transaction entity = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        TransactionStatus currentStatus = entity.getStatus();
        TransactionStatus requestedStatus = dto.getStatus();

        if (currentStatus.isTerminal()) {
            throw new InvalidStateTransitionException(currentStatus, requestedStatus);
        }

        entity.setStatus(requestedStatus);
        Transaction updated = transactionRepository.saveAndFlush(entity);

        log.info("Transaction {} status updated: {} -> {}", transactionId, currentStatus, requestedStatus);

        return mapToResponseDTO(updated);
    }

    /**
     * Task D: Get customer transactions
     * Fetches all transactions for a given customer ID. Returns empty list if none found.
     */
    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getCustomerTransactions(String customerId) {
        log.debug("Fetching transactions for customer: {}", customerId);

        return transactionRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private Transaction mapToEntity(TransactionRequestDTO dto) {
        Transaction entity = new Transaction();
        entity.setTransactionId(dto.getTransactionId());
        entity.setCustomerId(dto.getCustomerId());
        entity.setAmount(dto.getAmount());
        entity.setCurrency(dto.getCurrency());
        entity.setType(dto.getType());
        return entity;
    }

    private TransactionResponseDTO mapToResponseDTO(Transaction entity) {
        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.setTransactionId(entity.getTransactionId());
        dto.setCustomerId(entity.getCustomerId());
        dto.setAmount(entity.getAmount());
        dto.setCurrency(entity.getCurrency());
        dto.setType(entity.getType());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
