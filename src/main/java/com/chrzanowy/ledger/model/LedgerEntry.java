package com.chrzanowy.ledger.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public record LedgerEntry(UUID leaseId, OffsetDateTime entryDate, OffsetDateTime calculationDate, String description, String currency, BigDecimal debit,
                          BigDecimal credit, BigDecimal balance) {

    public static LedgerEntry from(LedgerEntity ledgerEntity) {
        return new LedgerEntry(ledgerEntity.leaseId(), ledgerEntity.entryDate(), ledgerEntity.calculationDate(), ledgerEntity.description(),
            ledgerEntity.currency(),
            ledgerEntity.debit(), ledgerEntity.credit(), ledgerEntity.balance());
    }

    public static LedgerEntry empty(UUID leaseId, LocalDate date) {
        return new LedgerEntry(leaseId, date.atStartOfDay().atOffset(ZoneOffset.UTC), null, "INITIAL", "USD",
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public static LedgerEntry debit(UUID leaseId, OffsetDateTime date, String description, String currency, BigDecimal amount, BigDecimal balance) {
        return new LedgerEntry(leaseId, date, null, description, currency, amount, BigDecimal.ZERO, balance);
    }

    public static LedgerEntry credit(UUID leaseId, OffsetDateTime date, String description, String currency, BigDecimal amount, BigDecimal balance) {
        return new LedgerEntry(leaseId, date, null, description, currency, BigDecimal.ZERO, amount, balance);
    }
}
