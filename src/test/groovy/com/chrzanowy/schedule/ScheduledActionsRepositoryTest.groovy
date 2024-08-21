package com.chrzanowy.schedule

import com.chrzanowy.BaseIntegrationSpec
import com.chrzanowy.schedule.model.ActionType
import com.chrzanowy.schedule.model.ScheduledAction
import org.springframework.beans.factory.annotation.Autowired

import java.time.OffsetDateTime
import java.time.ZoneOffset

class ScheduledActionsRepositoryTest extends BaseIntegrationSpec {

    @Autowired
    private ScheduledActionsRepository scheduledActionsRepository

    def "should insert scheduled action and find it afterwards"() {
        given:
        var scheduledDate = OffsetDateTime.now(ZoneOffset.UTC).plusDays(30)

        when:
        scheduledActionsRepository.insert(new ScheduledAction(UUID.randomUUID(), scheduledDate, ActionType.RECALCULATE_LEDGER))
        scheduledActionsRepository.insert(new ScheduledAction(UUID.randomUUID(), scheduledDate, ActionType.RECALCULATE_LEDGER))

        then:
        scheduledActionsRepository.findAllScheduledLedgersForDay(scheduledDate.toLocalDate()).size() == 2
    }
}
