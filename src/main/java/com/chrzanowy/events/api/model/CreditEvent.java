package com.chrzanowy.events.api.model;

import com.chrzanowy.model.CreditFeeGroup;
import com.chrzanowy.model.CreditFeeType;
import com.chrzanowy.model.Currency;
import com.chrzanowy.model.EventType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class CreditEvent extends BaseEvent {

    @NotNull
    private final LocalDate effectiveOnDate;

    @NotNull
    private final CreditFeeGroup feeGroup;

    @NotNull
    private final CreditFeeType feeType;

    @NotEmpty
    private final String description;

    public CreditEvent(EventType eventType, UUID eventUuid, OffsetDateTime eventTime, Long amount, Currency currency,
        UUID leaseUuid, LocalDate effectiveOnDate, CreditFeeGroup feeGroup, CreditFeeType feeType, String description) {
        super(eventType, eventUuid, eventTime, amount, currency, leaseUuid);
        this.effectiveOnDate = effectiveOnDate;
        this.feeGroup = feeGroup;
        this.feeType = feeType;
        this.description = description;
    }
}
