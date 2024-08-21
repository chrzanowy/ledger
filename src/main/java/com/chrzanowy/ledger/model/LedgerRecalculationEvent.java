package com.chrzanowy.ledger.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@ToString
@EqualsAndHashCode(callSuper = false)
@Getter
public class LedgerRecalculationEvent extends ApplicationEvent {

    private final UUID leaseId;

    private final OffsetDateTime lastUpdate;

    public LedgerRecalculationEvent(UUID leaseId, OffsetDateTime lastUpdate) {
        super("internal");
        this.leaseId = leaseId;
        this.lastUpdate = lastUpdate;
    }
}
