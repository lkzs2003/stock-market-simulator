package org.example.market.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private String transactionId;
    private FinancialInstrument instrument;
    private BigDecimal quantity;
    private String transactionType;
    private BigDecimal transactionPrice;
    private LocalDateTime transactionDate;
    private TransactionStatus status;

    public Transaction(String transactionType, FinancialInstrument instrument, BigDecimal quantity, BigDecimal transactionPrice, LocalDateTime transactionDate) {
        this.transactionType = transactionType;
        this.instrument = instrument;
        this.quantity = quantity;
        this.transactionPrice = transactionPrice;
        this.transactionDate = transactionDate;
        this.status = TransactionStatus.PENDING;
    }

    public boolean execute() {
        if (!validate()) {
            this.status = TransactionStatus.FAILED;
            return false;
        }
        // Implementacja logiki wykonania transakcji
        this.status = TransactionStatus.COMPLETED;
        return true;
    }

    private boolean validate() {
        // Walidacja warunków transakcji
        return true;
    }

    public void rollback() {
        // Implementacja logiki cofania transakcji
        this.status = TransactionStatus.PENDING;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public FinancialInstrument getInstrument() {
        return instrument;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public BigDecimal getTransactionPrice() {
        return transactionPrice;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public TransactionStatus getStatus() {
        return status;
    }
}

