package com.chrzanowy.events.api.model;

import com.chrzanowy.model.Currency;
import com.chrzanowy.model.EventType;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ChargeEvent extends BaseEvent {

    private final LocalDate dueDate;

    private final String feeGroup;

    private final String feeType;

    private final LocalDate serviceDateStart;

    private final LocalDate serviceDateEnd;

    private final String description;

    public ChargeEvent(EventType eventType, UUID eventUuid, OffsetDateTime eventTime, Long amount, Currency currency,
        UUID leaseUuid, LocalDate dueDate, String feeGroup, String feeType, LocalDate serviceDateStart, LocalDate serviceDateEnd,
        String description) {
        super(eventType, eventUuid, eventTime, amount, currency, leaseUuid);
        this.dueDate = dueDate;
        this.feeGroup = feeGroup;
        this.feeType = feeType;
        this.serviceDateStart = serviceDateStart;
        this.serviceDateEnd = serviceDateEnd;
        this.description = description;
    }
}
