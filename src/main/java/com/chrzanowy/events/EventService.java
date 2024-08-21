package com.chrzanowy.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.chrzanowy.events.api.model.BaseEvent;
import com.chrzanowy.events.api.model.ChargeEvent;
import com.chrzanowy.events.api.model.CreditEvent;
import com.chrzanowy.events.api.model.PaymentEvent;
import com.chrzanowy.events.api.exception.InvalidEventException;
import com.chrzanowy.events.model.EventEntity;
import com.chrzanowy.transactions.model.Transaction;
import com.chrzanowy.transactions.model.TransactionEvent;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class EventService {

    private final ObjectMapper objectMapper;

    private final EventRepository eventRepository;

    private final ApplicationEventMulticaster applicationEventMulticaster;

    public <E extends BaseEvent> void handle(E event) {
        boolean validEvent = eventRepository.insert(serializeEvent(event));
        if (!validEvent) {
            log.warn("Event {} already processed", event.getEventUuid());
            return;
        }
        sendEvent(event);
    }

    private <E extends BaseEvent> void sendEvent(E event) {
        switch (event) {
            case CreditEvent e -> sendEvent(Transaction.fromEvent(e));
            case ChargeEvent e -> sendEvent(Transaction.fromEvent(e));
            case PaymentEvent e -> sendEvent(Transaction.fromEvent(e));
            default -> {
                //do nothing, take it gracefully
            }
        }
    }

    private void sendEvent(Transaction transaction) {
        applicationEventMulticaster.multicastEvent(new TransactionEvent(transaction));
    }

    private <E extends BaseEvent> EventEntity serializeEvent(E event) {
        try {
            return new EventEntity(event.getEventUuid(), event.getEventTime(), objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            throw new InvalidEventException();
        }
    }

    private BaseEvent deserializeEvent(EventEntity event) {
        try {
            return objectMapper.readValue(event.eventBody(), BaseEvent.class);
        } catch (Exception e) {
            throw new InvalidEventException();
        }
    }

    public void reprocessEvent(UUID eventId) {
        eventRepository.findByEventUuid(eventId)
            .ifPresent(event -> sendEvent(deserializeEvent(event)));
    }
}
