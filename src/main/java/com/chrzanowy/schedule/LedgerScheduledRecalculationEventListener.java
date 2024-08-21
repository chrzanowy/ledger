package com.chrzanowy.schedule;

import com.chrzanowy.schedule.model.LedgerScheduledRecalculationEvent;
import com.chrzanowy.schedule.model.ScheduledAction;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LedgerScheduledRecalculationEventListener {

    private final ScheduledActionsRepository repository;

    @EventListener
    public void handle(LedgerScheduledRecalculationEvent event) {
        repository.insert(ScheduledAction.from(event));
    }
}
