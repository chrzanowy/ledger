package com.chrzanowy.schedule.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@ToString
@EqualsAndHashCode(callSuper = false)
@Getter
public class LedgerScheduledRecalculationEvent extends ApplicationEvent {

    private final UUID leaseId;

    private final OffsetDateTime scheduledDate;

    public LedgerScheduledRecalculationEvent(UUID leaseId, OffsetDateTime scheduledDate) {
        super("internal");
        this.leaseId = leaseId;
        this.scheduledDate = scheduledDate;
    }
}
