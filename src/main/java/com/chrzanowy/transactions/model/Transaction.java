package com.chrzanowy.transactions.model;

import com.chrzanowy.events.api.model.ChargeEvent;
import com.chrzanowy.events.api.model.CreditEvent;
import com.chrzanowy.events.api.model.PaymentEvent;
import com.chrzanowy.model.TransactionStatus;
import com.chrzanowy.model.TransactionSubType;
import com.chrzanowy.model.TransactionType;
import com.chrzanowy.utils.MoneyParser;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import javax.money.MonetaryAmount;
import org.javamoney.moneta.FastMoney;

public record Transaction(UUID id, UUID externalId, UUID eventId, UUID leaseId, TransactionType type, TransactionSubType subType, MonetaryAmount amount,
                          MonetaryAmount fee, OffsetDateTime date, TransactionStatus status, String description) {

    public static Transaction fromEvent(CreditEvent event) {
        return new Transaction(null, null, event.getEventUuid(), event.getLeaseUuid(), TransactionType.CREDIT, TransactionSubType.of(event.getFeeGroup()),
            MoneyParser.parseDecimalAmount(event.getAmount(), event.getCurrency().name()),
            FastMoney.of(BigDecimal.ZERO, event.getCurrency().name()), event.getEffectiveOnDate().atStartOfDay().atOffset(ZoneOffset.UTC),
            TransactionStatus.SETTLED,
            event.getFeeType() + "-" + event.getDescription());
    }

    public static Transaction fromEvent(ChargeEvent event) {
        return new Transaction(null, null, event.getEventUuid(), event.getLeaseUuid(), TransactionType.CHARGE, TransactionSubType.BASE_CHARGE,
            MoneyParser.parseDecimalAmount(event.getAmount(), event.getCurrency().name()),
            FastMoney.of(BigDecimal.ZERO, event.getCurrency().name()), event.getDueDate().atStartOfDay().atOffset(ZoneOffset.UTC), TransactionStatus.SETTLED,
            event.getDescription());
    }

    public static Transaction fromEvent(PaymentEvent event) {
        return new Transaction(null, event.getPaymentUuid(), event.getEventUuid(), event.getLeaseUuid(), TransactionType.PAYMENT,
            TransactionSubType.BASE_PAYMENT,
            MoneyParser.parseDecimalAmount(event.getAmount(), event.getCurrency().name()),
            MoneyParser.parseDecimalAmount(event.getFee(), event.getCurrency().name()),
            event.getCreatedAt().withOffsetSameInstant(ZoneOffset.UTC), TransactionStatus.of(event.getStatus()), null);
    }

    public static Transaction fromEntity(TransactionEntity transactionEntity) {
        return new Transaction(transactionEntity.id(), transactionEntity.externalId(), transactionEntity.eventId(), transactionEntity.leaseId(),
            transactionEntity.type(),
            transactionEntity.subType(), FastMoney.of(transactionEntity.amount(), transactionEntity.currency()),
            FastMoney.of(transactionEntity.fee(), transactionEntity.currency()),
            transactionEntity.date(), transactionEntity.status(), transactionEntity.description());
    }
}
