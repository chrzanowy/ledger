package com.chrzanowy.schedule;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

import com.chrzanowy.schedule.model.ScheduledAction;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ScheduledActionsRepository {

    public static final Table<Record> TABLE = table("scheduled_actions");

    public static final Field<UUID> LEASE_ID = field(name("LEASE_ID"), UUID.class);

    public static final Field<OffsetDateTime> ACTION_DATE = field(name("ACTION_DATE"), OffsetDateTime.class);

    public static final Field<String> ACTION_TYPE = field(name("ACTION_TYPE"), String.class);

    private final DSLContext dslContext;

    public void insert(ScheduledAction from) {
        //due to limitation of H2 and JOOQ i can't use onConflictDoNothing() method so i have to check if record exists manually
        int count = dslContext.fetchCount(dslContext.select()
            .from(TABLE)
            .where(LEASE_ID.eq(from.leaseId()), ACTION_DATE.eq(from.scheduledDate()), ACTION_TYPE.eq(from.type().name())));

        if (count == 0) {
            dslContext.insertInto(TABLE)
                .columns(LEASE_ID, ACTION_DATE, ACTION_TYPE)
                .values(from.leaseId(), from.scheduledDate(), from.type().name())
//                .onConflict(LEASE_ID, ACTION_DATE)
//                .doNothing()
                .execute();
        }
    }

    public Collection<ScheduledAction> findAllScheduledLedgersForDay(LocalDate dat) {
        return dslContext.select(LEASE_ID, ACTION_DATE, ACTION_TYPE)
            .from(TABLE)
            .where(ACTION_DATE.cast(LocalDate.class).eq(dat))
            .fetchInto(ScheduledAction.class);
    }
}
