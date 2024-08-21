package com.chrzanowy.schedule.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ScheduledAction(UUID leaseId, OffsetDateTime scheduledDate, ActionType type) {

    public static ScheduledAction from(LedgerScheduledRecalculationEvent event) {
        return new ScheduledAction(event.getLeaseId(), event.getScheduledDate(), ActionType.RECALCULATE_LEDGER);
    }

}
