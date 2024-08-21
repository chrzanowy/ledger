package com.chrzanowy.schedule

import com.chrzanowy.ledger.model.LedgerRecalculationEvent
import com.chrzanowy.schedule.model.ActionType
import com.chrzanowy.schedule.model.ScheduledAction
import org.springframework.context.event.ApplicationEventMulticaster
import spock.lang.Specification

import java.time.OffsetDateTime
import java.time.ZoneOffset

class ScheduledActionsListenerTest extends Specification {

    private ApplicationEventMulticaster applicationEventMulticaster = Mock(ApplicationEventMulticaster.class)

    private ScheduledActionsRepository repository = Mock(ScheduledActionsRepository.class)

    def "should find all events for day and send recalculation event for lease"() {
        given:
        var listener = new ScheduledActionsListener(applicationEventMulticaster, repository)
        var now = OffsetDateTime.now(ZoneOffset.UTC)
        var leaseId = UUID.randomUUID()
        var leaseId2 = UUID.randomUUID()
        var event = new ScheduledAction(leaseId, now, ActionType.RECALCULATE_LEDGER)
        var event2 = new ScheduledAction(leaseId2, now, ActionType.RECALCULATE_LEDGER)
        repository.findAllScheduledLedgersForDay(now.toLocalDate()) >> [event, event2]

        when:
        listener.runScheduledLedgers()

        then:
        with(applicationEventMulticaster) {
            1 * multicastEvent({ ev -> ev == new LedgerRecalculationEvent(leaseId, now) })
            1 * multicastEvent({ ev -> ev == new LedgerRecalculationEvent(leaseId2, now) })
        }
    }
}
