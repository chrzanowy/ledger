package com.chrzanowy.events;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JSON;
import org.jooq.Record;
import org.jooq.Table;
import org.springframework.stereotype.Repository;
import com.chrzanowy.events.model.EventEntity;

@Repository
@RequiredArgsConstructor
public class EventRepository {

    public static final Table<Record> TABLE = table("events");

    public static final Field<UUID> EVENT_UUID = field(name("EVENT_UUID"), UUID.class);

    public static final Field<OffsetDateTime> EVENT_TIME = field(name("EVENT_TIME"), OffsetDateTime.class);

    public static final Field<JSON> EVENT_BODY = field(name("EVENT_BODY"), JSON.class);

    private final DSLContext dslContext;

    public boolean insert(EventEntity event) {
        // workaround for h2, we should not insert duplicate events
        int eventIdCount = dslContext.fetchCount(dslContext.selectFrom(TABLE)
            .where(EVENT_UUID.eq(event.eventUuid())));
        if (eventIdCount > 0) {
            return false;
        }
        dslContext.insertInto(TABLE)
            .columns(EVENT_UUID, EVENT_TIME, EVENT_BODY)
            .values(event.eventUuid(), event.eventTime(), JSON.valueOf(event.eventBody()))
//            .onDuplicateKeyIgnore()
            .execute();
        return true;
    }

    public Optional<EventEntity> findByEventUuid(UUID eventId) {
        return dslContext.selectFrom(TABLE)
            .where(EVENT_UUID.eq(eventId))
            .fetchOptionalInto(EventEntity.class);
    }
}
