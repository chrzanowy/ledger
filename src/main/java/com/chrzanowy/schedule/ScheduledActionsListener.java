package com.chrzanowy.schedule;

import com.chrzanowy.ledger.model.LedgerRecalculationEvent;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduledActionsListener {

    private final ApplicationEventMulticaster applicationEventMulticaster;

    private final ScheduledActionsRepository repository;

    @Scheduled(cron = "0 0 0 * * *", zone = "UTC")
    public void runScheduledLedgers() {
        var today = OffsetDateTime.now(ZoneOffset.UTC).toLocalDate();
        repository.findAllScheduledLedgersForDay(today)
            .forEach(action -> applicationEventMulticaster.multicastEvent(new LedgerRecalculationEvent(action.leaseId(), action.scheduledDate())));
    }

}
