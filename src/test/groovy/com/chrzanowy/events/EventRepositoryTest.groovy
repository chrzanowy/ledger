package com.chrzanowy.events

import com.chrzanowy.BaseIntegrationSpec
import com.chrzanowy.events.model.EventEntity
import org.springframework.beans.factory.annotation.Autowired

import java.time.OffsetDateTime
import java.time.ZoneOffset

class EventRepositoryTest extends BaseIntegrationSpec {

    @Autowired
    private EventRepository eventRepository

    def "should insert event and find it afterwards"() {
        given:
        var id = UUID.randomUUID()
        var event = new EventEntity(id, OffsetDateTime.now(ZoneOffset.UTC), "{}")

        when:
        var inserted = eventRepository.insert(event)
        var foundEvent = eventRepository.findByEventUuid(id)

        then:
        foundEvent.isPresent()
        inserted
        foundEvent.get() == event
    }

    def "should not insert event with same id twice"() {
        given:
        var id = UUID.randomUUID()
        var event = new EventEntity(id, OffsetDateTime.now(ZoneOffset.UTC), "{}")

        when:
        var inserted = eventRepository.insert(event)
        var insertedDuplicate = eventRepository.insert(event)

        then:
        inserted
        !insertedDuplicate
    }
}
