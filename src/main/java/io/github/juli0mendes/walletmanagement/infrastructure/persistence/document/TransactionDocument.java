package io.github.juli0mendes.walletmanagement.infrastructure.persistence.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "transactions")
public class TransactionDocument {

    @Id
    private String id;

    @Indexed
    private String walletId;

    @Indexed
    private String transferId;

    @Indexed
    private String operationId;

    private String type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String description;
    private LocalDateTime createdAt;

    public TransactionDocument() {
    }

    public TransactionDocument(String id, String walletId, String type, BigDecimal amount,
                               BigDecimal balanceAfter, String description, LocalDateTime createdAt) {
        this(id, walletId, null, null, type, amount, balanceAfter, description, createdAt);
    }

    public TransactionDocument(String id, String walletId, String transferId, String operationId,
                               String type, BigDecimal amount, BigDecimal balanceAfter,
                               String description, LocalDateTime createdAt) {
        this.id = id;
        this.walletId = walletId;
        this.transferId = transferId;
        this.operationId = operationId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getWalletId() { return walletId; }
    public void setWalletId(String walletId) { this.walletId = walletId; }

    public String getTransferId() { return transferId; }
    public void setTransferId(String transferId) { this.transferId = transferId; }

    public String getOperationId() { return operationId; }
    public void setOperationId(String operationId) { this.operationId = operationId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

