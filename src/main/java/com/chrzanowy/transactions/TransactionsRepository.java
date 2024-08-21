package com.chrzanowy.transactions;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

import com.chrzanowy.model.TransactionStatus;
import com.chrzanowy.model.TransactionSubType;
import com.chrzanowy.model.TransactionType;
import com.chrzanowy.transactions.model.TransactionEntity;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class TransactionsRepository {

    public static final Table<Record> TABLE = table("transactions");

    public static final Field<UUID> ID = field(name("ID"), UUID.class);

    public static final Field<UUID> EXTERNAL_ID = field(name("EXTERNAL_ID"), UUID.class);

    public static final Field<UUID> LEASE_ID = field(name("LEASE_ID"), UUID.class);

    public static final Field<UUID> EVENT_ID = field(name("EVENT_ID"), UUID.class);

    public static final Field<String> CURRENCY = field(name("CURRENCY"), String.class);

    public static final Field<String> TRANSACTION_TYPE = field(name("TRANSACTION_TYPE"), String.class);

    public static final Field<String> TRANSACTION_SUB_TYPE = field(name("TRANSACTION_SUB_TYPE"), String.class);

    public static final Field<BigDecimal> AMOUNT = field(name("BASE_AMOUNT"), BigDecimal.class);

    public static final Field<BigDecimal> FEE = field(name("FEE_AMOUNT"), BigDecimal.class);

    public static final Field<OffsetDateTime> CREATED_AT = field(name("CREATED_AT"), OffsetDateTime.class);

    public static final Field<String> STATUS = field(name("STATUS"), String.class);

    public static final Field<String> DESCRIPTION = field(name("DESCRIPTION"), String.class);

    private final DSLContext dslContext;

    public boolean insert(TransactionEntity transaction) {
        int countByEventId = dslContext.fetchCount(dslContext.selectFrom(TABLE).where(EVENT_ID.eq(transaction.eventId())));
        if (countByEventId > 0) {
            log.warn("Transaction with event id {} already exists", transaction.eventId());
            return false;
        }
        dslContext.insertInto(TABLE)
            .columns(ID, EXTERNAL_ID, LEASE_ID, EVENT_ID, CURRENCY, TRANSACTION_TYPE, TRANSACTION_SUB_TYPE, AMOUNT, FEE, CREATED_AT, STATUS, DESCRIPTION)
            .values(UUID.randomUUID(), transaction.externalId(), transaction.leaseId(), transaction.eventId(), transaction.currency(),
                transaction.type().name(), transaction.subType().name(), transaction.amount(), transaction.fee(), transaction.date(),
                transaction.status().name(), transaction.description())
//            .onConflict(EVENT_ID)
//            .doNothing()
            .execute();
        return true;
    }


    private TransactionEntity mapRecordToEntity(Record record) {
        return new TransactionEntity(record.get(ID), record.get(EXTERNAL_ID), record.get(LEASE_ID), record.get(EVENT_ID),
            TransactionType.valueOf(record.get(TRANSACTION_TYPE)), TransactionSubType.valueOf(record.get(TRANSACTION_SUB_TYPE)), record.get(AMOUNT),
            record.get(FEE), record.get(CURRENCY), record.get(CREATED_AT), TransactionStatus.valueOf(record.get(STATUS)), record.get(DESCRIPTION));
    }

    public Collection<TransactionEntity> findAllByLeaseId(UUID leaseId, OffsetDateTime since, int offset, int limit) {
        return dslContext.selectFrom(TABLE)
            .where(LEASE_ID.eq(leaseId))
            .and(CREATED_AT.greaterOrEqual(since))
            .orderBy(CREATED_AT.desc())
            .offset(offset)
            .limit(limit)
            .fetch()
            .map(this::mapRecordToEntity);
    }

    public Collection<TransactionEntity> findAllByLeaseIdSinceExcludingCancelled(UUID leaseId, OffsetDateTime since) {
        return dslContext.selectFrom(TABLE)
            .where(LEASE_ID.eq(leaseId))
            .and(CREATED_AT.greaterOrEqual(since))
            .and(STATUS.eq(TransactionStatus.SETTLED.name()))
            .orderBy(CREATED_AT.desc())
            .fetch()
            .map(this::mapRecordToEntity);
    }
}
