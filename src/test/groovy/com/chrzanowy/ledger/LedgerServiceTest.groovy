package com.chrzanowy.ledger

import com.chrzanowy.ledger.model.LedgerEntity
import com.chrzanowy.ledger.model.LedgerEntry
import com.chrzanowy.ledger.model.LedgerRecalculationEvent
import com.chrzanowy.model.TransactionStatus
import com.chrzanowy.model.TransactionSubType
import com.chrzanowy.model.TransactionType
import com.chrzanowy.transactions.model.Transaction
import com.chrzanowy.transactions.TransactionsService
import org.javamoney.moneta.FastMoney
import org.springframework.context.event.ApplicationEventMulticaster
import spock.lang.Specification

import java.time.OffsetDateTime
import java.time.ZoneOffset

class LedgerServiceTest extends Specification {

    private TransactionsService transactionsService = Mock(TransactionsService.class)

    private LedgerRepository ledgerRepository = Mock(LedgerRepository.class)

    private ApplicationEventMulticaster applicationEventMulticaster = Mock(ApplicationEventMulticaster.class)

    def "should send ledger recalculation event"() {
        given:
        var ledgerService = new LedgerService(transactionsService, ledgerRepository, applicationEventMulticaster)
        var leaseId = UUID.randomUUID()

        when:
        ledgerService.sendRecalculationEvent(leaseId);

        then:
        1 * applicationEventMulticaster.multicastEvent({
            LedgerRecalculationEvent e ->
                e.leaseId == leaseId
        })
    }

    def "should get ledger for lease"() {
        given:
        var ledgerService = new LedgerService(transactionsService, ledgerRepository, applicationEventMulticaster)
        var leaseId = UUID.randomUUID()
        var now = OffsetDateTime.now(ZoneOffset.UTC)
        var entity = new LedgerEntity(leaseId, now, null, "transaction", "USD", 100.0, 100.0, 100.0)
        ledgerRepository.findAllByLeaseId(leaseId, now.toLocalDate(), 0, 20) >> { [entity] }

        when:
        var foundLedger = ledgerService.getLedgerForLease(leaseId, now.toLocalDate(), 0, 20)

        then:
        with(foundLedger) {
            size() == 1
            first() == LedgerEntry.from(entity)
        }
    }

    def "should recalculate ledger"() {
        given:
        var ledgerService = new LedgerService(transactionsService, ledgerRepository, applicationEventMulticaster)
        var leaseId = UUID.randomUUID()
        var recalculationDate = OffsetDateTime.now(ZoneOffset.UTC).minusDays(10)
        var transaction1 = new Transaction(UUID.randomUUID(), UUID.randomUUID(), leaseId, UUID.randomUUID(),
                TransactionType.CHARGE, TransactionSubType.BASE_CHARGE, FastMoney.of(1900, "USD"),
                FastMoney.of(100, "USD"), recalculationDate, TransactionStatus.SETTLED, "rent #1")
        var transaction2 = new Transaction(UUID.randomUUID(), UUID.randomUUID(), leaseId, UUID.randomUUID(),
                TransactionType.PAYMENT, TransactionSubType.BASE_PAYMENT, FastMoney.of(1900, "USD"),
                FastMoney.of(100, "USD"), recalculationDate, TransactionStatus.SETTLED, "payment rent #1")

        var transaction3 = new Transaction(UUID.randomUUID(), UUID.randomUUID(), leaseId, UUID.randomUUID(),
                TransactionType.CREDIT, TransactionSubType.CREDIT_CORRECTION, FastMoney.of(0, "USD"),
                FastMoney.of(100, "USD"), recalculationDate.plusDays(1), TransactionStatus.SETTLED, "rent #1 correction")
        var transaction4 = new Transaction(UUID.randomUUID(), UUID.randomUUID(), leaseId, UUID.randomUUID(),
                TransactionType.CREDIT, TransactionSubType.CREDIT_MARKETING, FastMoney.of(50, "USD"),
                FastMoney.of(0, "USD"), recalculationDate.plusDays(1), TransactionStatus.SETTLED, "voucher for correction")

        var transaction5 = new Transaction(UUID.randomUUID(), UUID.randomUUID(), leaseId, UUID.randomUUID(),
                TransactionType.CHARGE, TransactionSubType.BASE_CHARGE, FastMoney.of(1900, "USD"),
                FastMoney.of(0, "USD"), recalculationDate.plusDays(5), TransactionStatus.SETTLED, "rent #2")
        var transaction6 = new Transaction(UUID.randomUUID(), UUID.randomUUID(), leaseId, UUID.randomUUID(),
                TransactionType.CHARGE, TransactionSubType.BASE_CHARGE, FastMoney.of(128, "USD"),
                FastMoney.of(13, "USD"), recalculationDate.plusDays(5), TransactionStatus.SETTLED, "electricity advance #1")

        var transaction7 = new Transaction(UUID.randomUUID(), UUID.randomUUID(), leaseId, UUID.randomUUID(),
                TransactionType.PAYMENT, TransactionSubType.BASE_PAYMENT, FastMoney.of(1900, "USD"),
                FastMoney.of(0, "USD"), recalculationDate.plusDays(7), TransactionStatus.SETTLED, "payment rent #2")

        var transaction8 = new Transaction(UUID.randomUUID(), UUID.randomUUID(), leaseId, UUID.randomUUID(),
                TransactionType.PAYMENT, TransactionSubType.BASE_PAYMENT, FastMoney.of(150, "USD"),
                FastMoney.of(0, "USD"), recalculationDate.plusDays(8), TransactionStatus.SETTLED, "electricity payment #1")

        var transaction9 = new Transaction(UUID.randomUUID(), UUID.randomUUID(), leaseId, UUID.randomUUID(),
                TransactionType.CREDIT, TransactionSubType.CREDIT_MARKETING, FastMoney.of(20, "USD"),
                FastMoney.of(0, "USD"), recalculationDate.plusDays(10), TransactionStatus.SETTLED, "voucher for card payment")
        var savedLedgers = new ArrayList<LedgerEntity>()

        transactionsService.findAllByLeaseIdSinceExcludingCancelled(leaseId, recalculationDate) >> {
            [transaction1, transaction2, transaction3, transaction4,
             transaction5, transaction6, transaction7, transaction8,
             transaction9]
        }
        ledgerRepository.findLastLedgerForDay(leaseId, recalculationDate.toLocalDate()) >> { Optional.empty() }
        ledgerRepository.findLastLedgerForDay(leaseId, recalculationDate.plusDays(1).toLocalDate()) >> { Optional.of(savedLedgers.get(1)) }
        ledgerRepository.findLastLedgerForDay(leaseId, recalculationDate.plusDays(5).toLocalDate()) >> { Optional.of(savedLedgers.get(3)) }
        ledgerRepository.findLastLedgerForDay(leaseId, recalculationDate.plusDays(7).toLocalDate()) >> { Optional.of(savedLedgers.get(5)) }
        ledgerRepository.findLastLedgerForDay(leaseId, recalculationDate.plusDays(8).toLocalDate()) >> { Optional.of(savedLedgers.get(6)) }
        ledgerRepository.findLastLedgerForDay(leaseId, recalculationDate.plusDays(10).toLocalDate()) >> { Optional.of(savedLedgers.get(7)) }
        ledgerRepository.insert(_ as LedgerEntity) >> { args -> savedLedgers.add(args[0]) }

        when:
        ledgerService.recalculateLedger(leaseId, recalculationDate)

        then:
        savedLedgers.size() == 9
        savedLedgers[8].balance() == 179
    }

}
