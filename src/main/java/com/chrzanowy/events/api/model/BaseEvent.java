package com.chrzanowy.events.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.chrzanowy.model.Currency;
import com.chrzanowy.model.EventType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ChargeEvent.class, name = "ChargeEvent"),
    @JsonSubTypes.Type(value = PaymentEvent.class, name = "PaymentEvent"),
    @JsonSubTypes.Type(value = CreditEvent.class, name = "CreditEvent")}
)
public abstract class BaseEvent implements Serializable {

    @NotNull
    private final EventType eventType;

    @NotNull
    private final UUID eventUuid;

    @NotNull
    private final OffsetDateTime eventTime;

    @Positive
    private final Long amount;

    @NotNull
    private final Currency currency;

    @NotNull
    private final UUID leaseUuid;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public BaseEvent(@NotNull @JsonProperty("eventType") EventType eventType, @NotEmpty @JsonProperty("eventUuid") UUID eventUuid,
        @NotNull @JsonProperty("eventTime") OffsetDateTime eventTime, @Positive @JsonProperty("amount") Long amount,
        @NotEmpty @Size(min = 3, max = 3) @JsonProperty("currency") Currency currency, @NotEmpty @JsonProperty("leaseUuid") UUID leaseUuid) {
        this.eventType = eventType;
        this.eventUuid = eventUuid;
        this.eventTime = eventTime;
        this.amount = amount;
        this.currency = currency;
        this.leaseUuid = leaseUuid;
    }
}
