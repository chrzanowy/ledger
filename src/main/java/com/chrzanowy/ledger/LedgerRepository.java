package com.chrzanowy.ledger;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

import com.chrzanowy.ledger.model.LedgerEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LedgerRepository {

    public static final Table<Record> TABLE = table("ledgers");

    public static final Field<UUID> LEASE_ID = field(name("LEASE_ID"), UUID.class);

    public static final Field<String> CURRENCY = field(name("CURRENCY"), String.class);

    public static final Field<BigDecimal> DEBIT = field(name("DEBIT"), BigDecimal.class);

    public static final Field<BigDecimal> CREDIT = field(name("CREDIT"), BigDecimal.class);

    public static final Field<BigDecimal> BALANCE = field(name("BALANCE"), BigDecimal.class);

    public static final Field<OffsetDateTime> ENTRY_DATE = field(name("ENTRY_DATE"), OffsetDateTime.class);

    public static final Field<OffsetDateTime> CALCULATION_DATE = field(name("CALCULATION_DATE"), OffsetDateTime.class);

    public static final Field<String> DESCRIPTION = field(name("DESCRIPTION"), String.class);

    private final DSLContext dslContext;

    public Optional<LedgerEntity> findLastLedgerForDay(UUID leaseId, LocalDate date) {
        return dslContext.selectFrom(TABLE)
            .where(LEASE_ID.eq(leaseId))
            .and(ENTRY_DATE.cast(LocalDate.class).lessOrEqual(date))
            .orderBy(CALCULATION_DATE.desc(), ENTRY_DATE.desc())
            .limit(1)
            .fetchOptionalInto(LedgerEntity.class);
    }

    public void insert(LedgerEntity ledgerTransaction) {
        dslContext.insertInto(TABLE)
            .columns(LEASE_ID, ENTRY_DATE, CALCULATION_DATE, DESCRIPTION, CURRENCY, DEBIT, CREDIT, BALANCE)
            .values(ledgerTransaction.leaseId(), ledgerTransaction.entryDate(), OffsetDateTime.now(ZoneOffset.UTC), ledgerTransaction.description(),
                ledgerTransaction.currency(), ledgerTransaction.debit(), ledgerTransaction.credit(), ledgerTransaction.balance())
            .execute();
    }

    public List<LedgerEntity> findAllByLeaseId(UUID leaseId, LocalDate atDate, int offset, int max) {
        return dslContext.selectFrom(TABLE)
            .where(LEASE_ID.eq(leaseId))
            .and(ENTRY_DATE.cast(LocalDate.class).lessOrEqual(atDate))
            .orderBy(CALCULATION_DATE.desc(), ENTRY_DATE.desc())
            .offset(offset)
            .limit(max)
            .fetchInto(LedgerEntity.class);
    }

    public void deleteAllByLeaseIdAfter(UUID leaseId, LocalDate after) {
        dslContext.deleteFrom(TABLE)
            .where(LEASE_ID.eq(leaseId))
            .and(ENTRY_DATE.cast(LocalDate.class).greaterOrEqual(after))
            .execute();
    }
}
