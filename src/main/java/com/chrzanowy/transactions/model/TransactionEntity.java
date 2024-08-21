package com.chrzanowy.transactions.model;

import com.chrzanowy.model.TransactionStatus;
import com.chrzanowy.model.TransactionSubType;
import com.chrzanowy.model.TransactionType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TransactionEntity(UUID id, UUID externalId, UUID leaseId, UUID eventId, TransactionType type, TransactionSubType subType, BigDecimal amount,
                                BigDecimal fee, String currency, OffsetDateTime date, TransactionStatus status, String description) {

    public static TransactionEntity fromTransaction(Transaction transaction) {
        return new TransactionEntity(transaction.id(), transaction.externalId(), transaction.leaseId(), transaction.eventId(), transaction.type(),
            transaction.subType(), transaction.amount().getNumber().numberValue(BigDecimal.class), transaction.fee().getNumber().numberValue(BigDecimal.class),
            transaction.amount().getCurrency().getCurrencyCode(), transaction.date(), transaction.status(), transaction.description());
    }
}
