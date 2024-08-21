package com.chrzanowy.ledger.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record LedgerEntity(UUID leaseId, OffsetDateTime entryDate, OffsetDateTime calculationDate, String description, String currency, BigDecimal debit,
                           BigDecimal credit, BigDecimal balance) {

    public static LedgerEntity from(LedgerEntry ledgerEntry) {
        return new LedgerEntity(ledgerEntry.leaseId(), ledgerEntry.entryDate(), ledgerEntry.calculationDate(), ledgerEntry.description(),
            ledgerEntry.currency(), ledgerEntry.debit(), ledgerEntry.credit(), ledgerEntry.balance());
    }
}
