package com.chrzanowy.ledger

import com.chrzanowy.ledger.model.LedgerRecalculationEvent
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.time.OffsetDateTime
import java.time.ZoneOffset

class LedgerEventListenerTest extends Specification {

    private LedgerService ledgerService = Mock(LedgerService.class)

    def "should recalculate ledger on event handle"() {
        given:
        var ledgerEventListener = new LedgerEventListener(ledgerService)
        var counter = 0
        var now = OffsetDateTime.now(ZoneOffset.UTC)
        var leaseId = UUID.randomUUID()
        var recalculationEvent = new LedgerRecalculationEvent(leaseId, now)
        ledgerService.recalculateLedger(leaseId, now) >> {
            counter++
        }

        when:
        ledgerEventListener.handle(recalculationEvent)

        then:
        new PollingConditions(timeout: 1).within(1, {
            counter == 1
        })
    }

}
