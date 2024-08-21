package com.chrzanowy.schedule

import com.chrzanowy.schedule.model.LedgerScheduledRecalculationEvent
import com.chrzanowy.schedule.model.ScheduledAction
import spock.lang.Specification

import java.time.OffsetDateTime
import java.time.ZoneOffset

class LedgerScheduledRecalculationEventListenerTest extends Specification {

    private ScheduledActionsRepository scheduledActionsRepository = Mock(ScheduledActionsRepository.class);

    def "should insert new action on event handle"() {
        given:
        var ledgerScheduledRecalculationEventListener = new LedgerScheduledRecalculationEventListener(scheduledActionsRepository)
        var now = OffsetDateTime.now(ZoneOffset.UTC)
        var leaseId = UUID.randomUUID()
        var event = new LedgerScheduledRecalculationEvent(leaseId, now)

        when:
        ledgerScheduledRecalculationEventListener.handle(event)

        then:
        1 * scheduledActionsRepository.insert({ action -> ScheduledAction.from(event) == action })
    }
}
