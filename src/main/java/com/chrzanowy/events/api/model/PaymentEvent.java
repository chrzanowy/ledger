package com.chrzanowy.events.api.model;

import com.chrzanowy.model.Currency;
import com.chrzanowy.model.EventType;
import com.chrzanowy.model.PaymentStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class PaymentEvent extends BaseEvent {

    @NotNull
    private final UUID paymentUuid;

    @NotEmpty
    private final String paymentProcessor;

    @NotNull
    private final PaymentStatus status;

    @PositiveOrZero
    private final Long fee;

    @NotNull
    private final OffsetDateTime createdAt;

    public PaymentEvent(EventType eventType, UUID eventUuid, OffsetDateTime eventTime, Long amount, Currency currency,
        UUID leaseUuid, UUID paymentUuid, String paymentProcessor, PaymentStatus status, Long fee, OffsetDateTime createdAt) {
        super(eventType, eventUuid, eventTime, amount, currency, leaseUuid);
        this.paymentUuid = paymentUuid;
        this.paymentProcessor = paymentProcessor;
        this.status = status;
        this.fee = fee;
        this.createdAt = createdAt;
    }
}
