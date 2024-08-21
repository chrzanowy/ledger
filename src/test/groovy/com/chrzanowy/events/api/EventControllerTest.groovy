package com.chrzanowy.events.api

import com.chrzanowy.RestIntegrationSpecTest
import com.chrzanowy.events.api.model.ChargeEvent
import com.chrzanowy.events.EventRepository
import com.chrzanowy.model.Currency
import com.chrzanowy.model.EventType
import org.springframework.beans.factory.annotation.Autowired

import java.time.OffsetDateTime
import java.time.ZoneOffset

class EventControllerTest extends RestIntegrationSpecTest {

    @Autowired
    private EventRepository eventRepository;

    def "should consume and save event"() {
        given:
        var now = OffsetDateTime.now(ZoneOffset.UTC)
        var eventId = UUID.randomUUID()

        when:
        given()
                .when()
                .body(new ChargeEvent(EventType.CHARGE, eventId, now, 10000, Currency.USD, UUID.randomUUID(), now.toLocalDate(),
                        "Rent & Fees", "Base rent", now.toLocalDate(), now.toLocalDate().plusDays(30), "April rent"))
                .post("/api/v1/events")
                .then()
                .statusCode(201)

        then:
        with(eventRepository.findByEventUuid(eventId)) {
            isPresent()
            get().eventUuid() == eventId
        }
    }

    def "should reprocess saved event"() {
        given:
        var now = OffsetDateTime.now(ZoneOffset.UTC)
        var eventId = UUID.randomUUID()

        when:
        given()
                .when()
                .body(new ChargeEvent(EventType.CHARGE, eventId, now, 10000, Currency.USD, UUID.randomUUID(), now.toLocalDate(),
                        "Rent & Fees", "Base rent", now.toLocalDate(), now.toLocalDate().plusDays(30), "April rent"))
                .post("/api/v1/events")
                .then()
                .statusCode(201)

        then:
        given()
                .when()
                .put("/api/v1/events/%s/reprocess".formatted(eventId))
                .then()
                .statusCode(204)
    }

}
