package com.chrzanowy.events

import com.fasterxml.jackson.databind.ObjectMapper
import com.chrzanowy.events.api.model.CreditEvent
import com.chrzanowy.events.api.model.PaymentEvent
import com.chrzanowy.configuration.JacksonConfiguration
import com.chrzanowy.events.model.EventEntity
import com.chrzanowy.model.CreditFeeGroup
import com.chrzanowy.model.CreditFeeType
import com.chrzanowy.model.Currency
import com.chrzanowy.model.EventType
import com.chrzanowy.model.PaymentStatus
import com.chrzanowy.transactions.model.Transaction
import com.chrzanowy.transactions.model.TransactionEvent
import org.springframework.context.event.ApplicationEventMulticaster
import spock.lang.Specification

import java.time.OffsetDateTime
import java.time.ZoneOffset

class EventServiceTest extends Specification {

    private ObjectMapper objectMapper = JacksonConfiguration.createObjectMapper()

    private EventRepository eventRepository = Mock(EventRepository.class)

    private ApplicationEventMulticaster applicationEventMulticaster = Mock(ApplicationEventMulticaster.class)

    def "should save event entity and emit event"() {
        given:
        var eventService = new EventService(objectMapper, eventRepository, applicationEventMulticaster)
        def now = OffsetDateTime.now(ZoneOffset.UTC)
        var eventId = UUID.randomUUID()
        var leaseId = UUID.randomUUID()
        var paymentId = UUID.randomUUID()
        var event = new PaymentEvent(EventType.PAYMENT, eventId, now, 10000, Currency.USD, leaseId, paymentId, "test", PaymentStatus.SUCCESS, 100, now);

        when:
        eventService.handle(event)

        then:
        1 * applicationEventMulticaster.multicastEvent({
            TransactionEvent e ->
                e.transaction == Transaction.fromEvent(event)
        })
        1 * eventRepository.insert({
            EventEntity e ->
                e.eventUuid() == eventId
                e.eventTime() == now
                e.eventBody() == ("""{"eventType":"PAYMENT","eventUuid":"%s","eventTime":"%s","amount":10000,"currency":"USD","leaseUuid":"%s","paymentUuid":"%s","paymentProcessor":"test","status":"SUCCESS","fee":100,"createdAt":"%s"}"""
                        .formatted(eventId, now, leaseId, paymentId, now))
        }) >> { true }
    }

    def "should reprocess event"() {
        given:
        var eventService = new EventService(objectMapper, eventRepository, applicationEventMulticaster)
        def now = OffsetDateTime.now(ZoneOffset.UTC)
        var eventId = UUID.randomUUID()
        var leaseId = UUID.randomUUID()
        var event = new CreditEvent(EventType.CREDIT, eventId, now, 10000, Currency.USD, leaseId, now.toLocalDate(), CreditFeeGroup.CORRECTIONS, CreditFeeType.SYSTEM_ERROR, "operator error");

        when:
        1 * eventRepository.findByEventUuid({
            UUID id ->
                id == eventId
        }) >> {
            Optional.of(new EventEntity(eventId, now, """{"eventType":"CREDIT","eventUuid":"%s","eventTime":"%s","amount":10000,"currency":"USD","leaseUuid":"%s","effectiveOnDate":"%s","feeGroup":"CORRECTIONS","feeType":"SYSTEM_ERROR","description":"operator error"}"""
                    .formatted(eventId, now, leaseId, now.toLocalDate())))
        }
        eventService.reprocessEvent(eventId)

        then:
        1 * applicationEventMulticaster.multicastEvent({
            TransactionEvent e ->
                e.transaction == Transaction.fromEvent(event)
        })
    }

}
