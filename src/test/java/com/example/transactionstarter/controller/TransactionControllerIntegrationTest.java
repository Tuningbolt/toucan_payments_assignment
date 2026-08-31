package com.example.transactionstarter.controller;

import com.example.transactionstarter.model.Transaction;
import com.example.transactionstarter.model.TransactionStatus;
import com.example.transactionstarter.model.TransactionType;
import com.example.transactionstarter.repository.TransactionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
    }

    // Helper: Valid creation JSON payload
    private String validCreatePayload(String txnId, String customerId) {
        return """
                {
                    "transactionId": "%s",
                    "customerId": "%s",
                    "amount": 150.75,
                    "currency": "USD",
                    "type": "DEPOSIT"
                }
                """.formatted(txnId, customerId);
    }

    // Helper: Seed a PENDING transaction in DB
    private Transaction insertPendingTransaction(String txnId, String customerId) {
        Transaction txn = new Transaction();
        txn.setTransactionId(txnId);
        txn.setCustomerId(customerId);
        txn.setAmount(new BigDecimal("100.00"));
        txn.setCurrency("USD");
        txn.setType(TransactionType.DEPOSIT);
        txn.setStatus(TransactionStatus.PENDING);
        return transactionRepository.save(txn);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. A transaction created successfully
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("1. A transaction created successfully — returns 201 Created")
    void createTransaction_success() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreatePayload("txn-001", "cust-001")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId", is("txn-001")))
                .andExpect(jsonPath("$.customerId", is("cust-001")))
                .andExpect(jsonPath("$.amount").value(150.75))
                .andExpect(jsonPath("$.currency", is("USD")))
                .andExpect(jsonPath("$.type", is("DEPOSIT")))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. A transaction rejected because it fails validation
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("2. A transaction rejected because it fails validation — returns 400 Bad Request")
    void createTransaction_validationFailure() throws Exception {
        String invalidPayload = """
                {
                    "transactionId": "",
                    "customerId": "",
                    "amount": -5.00,
                    "currency": "",
                    "type": null
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Failed")));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. A duplicate Transaction ID rejected
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("3. A duplicate Transaction ID rejected — returns 409 Conflict")
    void createTransaction_duplicate() throws Exception {
        // First insertion succeeds
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreatePayload("txn-dup", "cust-001")))
                .andExpect(status().isCreated());

        // Duplicate insertion with the same transactionId must be rejected
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreatePayload("txn-dup", "cust-001")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. A request for a transaction that does not exist
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("4. A request for a transaction that does not exist — returns 404 Not Found")
    void getTransaction_notFound() throws Exception {
        mockMvc.perform(get("/api/transactions/non-existent-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. A transaction retrieved successfully by ID (Bonus essential case)
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("5. A transaction retrieved successfully by ID — returns 200 OK")
    void getTransaction_success() throws Exception {
        insertPendingTransaction("txn-get", "cust-100");

        mockMvc.perform(get("/api/transactions/txn-get"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId", is("txn-get")))
                .andExpect(jsonPath("$.customerId", is("cust-100")))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. Transition from terminal state rejected (Bonus essential state guard)
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("6. Status update rejected when transaction is already in terminal state — returns 400 Bad Request")
    void updateStatus_terminalStateRejected() throws Exception {
        // Step 1: Insert and transition to COMPLETED (terminal)
        insertPendingTransaction("txn-term", "cust-300");
        mockMvc.perform(patch("/api/transactions/txn-term/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "COMPLETED" }
                                """))
                .andExpect(status().isOk());

        // Step 2: Attempt illegal transition from COMPLETED -> FAILED
        mockMvc.perform(patch("/api/transactions/txn-term/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "FAILED" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Invalid State Transition")));
    }
}
