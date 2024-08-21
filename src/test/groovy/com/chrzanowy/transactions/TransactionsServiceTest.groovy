package com.chrzanowy.transactions

import com.chrzanowy.ledger.model.LedgerRecalculationEvent
import com.chrzanowy.model.TransactionStatus
import com.chrzanowy.model.TransactionSubType
import com.chrzanowy.model.TransactionType
import com.chrzanowy.schedule.model.LedgerScheduledRecalculationEvent
import com.chrzanowy.transactions.model.Transaction
import com.chrzanowy.transactions.model.TransactionEntity
import org.javamoney.moneta.FastMoney
import org.springframework.context.event.ApplicationEventMulticaster
import spock.lang.Specification

import java.time.OffsetDateTime
import java.time.ZoneOffset

class TransactionsServiceTest extends Specification {

    private TransactionsRepository transactionsRepository = Mock(TransactionsRepository.class)

    private ApplicationEventMulticaster applicationEventMulticaster = Mock(ApplicationEventMulticaster.class)

    def "should insert new transaction and send ledger recalculation event"() {
        given:
        var transactionsService = new TransactionsService(transactionsRepository, applicationEventMulticaster)
        var now = OffsetDateTime.now(ZoneOffset.UTC)
        var leaseId = UUID.randomUUID()
        var transaction = new Transaction(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), leaseId,
                TransactionType.CREDIT, TransactionSubType.CREDIT_CORRECTION, FastMoney.of(200, "USD"),
                FastMoney.of(0, "USD"), now, TransactionStatus.SETTLED, "desc")

        when:
        transactionsService.processNewTransaction(transaction)

        then:
        1 * transactionsRepository.insert({ tr -> TransactionEntity.fromTransaction(transaction) == tr }) >> true
        1 * applicationEventMulticaster.multicastEvent({ ev -> new LedgerRecalculationEvent(leaseId, now) == ev })
    }

    def "should insert new transaction and schedule ledger recalculation event"() {
        given:
        var transactionsService = new TransactionsService(transactionsRepository, applicationEventMulticaster)
        var now = OffsetDateTime.now(ZoneOffset.UTC).plusDays(7)
        var leaseId = UUID.randomUUID()
        var transaction = new Transaction(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), leaseId,
                TransactionType.CREDIT, TransactionSubType.CREDIT_CORRECTION, FastMoney.of(200, "USD"),
                FastMoney.of(0, "USD"), now, TransactionStatus.SETTLED, "desc")

        when:
        transactionsService.processNewTransaction(transaction)

        then:
        1 * transactionsRepository.insert({ tr -> TransactionEntity.fromTransaction(transaction) == tr }) >> true
        1 * applicationEventMulticaster.multicastEvent({ ev -> new LedgerScheduledRecalculationEvent(leaseId, now) == ev })
    }

    def "should insert new transaction and schedule ledger recalculation event"() {
        given:
        var transactionsService = new TransactionsService(transactionsRepository, applicationEventMulticaster)
        var now = OffsetDateTime.now(ZoneOffset.UTC).plusDays(7)
        var leaseId = UUID.randomUUID()
        var transaction = new TransactionEntity(UUID.randomUUID(), UUID.randomUUID(), leaseId, UUID.randomUUID(),
                TransactionType.CREDIT, TransactionSubType.CREDIT_CORRECTION, 2500.0, 15.0, "USD", now, TransactionStatus.SETTLED, "desc")
        1 * transactionsRepository.findAllByLeaseIdSinceExcludingCancelled(leaseId, now) >> [transaction]
        1 * transactionsRepository.findAllByLeaseId(leaseId, now, 0, 20) >> [transaction]

        when:
        var foundTransactionsWithExclude = transactionsService.findAllByLeaseIdSinceExcludingCancelled(leaseId, now)
        var foundTransactions = transactionsService.findAllByLeaseIdSince(leaseId, now, 0, 20)

        then:
        with(foundTransactions) {
            size() == 1
            first() == Transaction.fromEntity(transaction)
        }
        foundTransactionsWithExclude == foundTransactions
    }
}
