package com.chrzanowy.ledger

import com.chrzanowy.BaseIntegrationSpec
import com.chrzanowy.ledger.model.LedgerEntity
import org.springframework.beans.factory.annotation.Autowired

import java.time.OffsetDateTime
import java.time.ZoneOffset

class LedgerRepositoryTest extends BaseIntegrationSpec {

    @Autowired
    private LedgerRepository ledgerRepository

    def "should insert ledger and find it afterwards"() {
        given:
        var now = OffsetDateTime.now(ZoneOffset.UTC)
        var leaseId = UUID.randomUUID()
        var ledgerEntity = new LedgerEntity(leaseId, now, null, "description", "USD", 100.0, 100.0, 100.0);

        when:
        ledgerRepository.insert(ledgerEntity)
        var foundLedgers = ledgerRepository.findAllByLeaseId(leaseId, now.toLocalDate(), 0, 20)

        then:
        foundLedgers.size() == 1

        expect:
        verifyAll {
            foundLedgers.first().leaseId() == ledgerEntity.leaseId()
            foundLedgers.first().entryDate() == ledgerEntity.entryDate()
            foundLedgers.first().calculationDate().isAfter(now)
            foundLedgers.first().description() == ledgerEntity.description()
            foundLedgers.first().currency() == ledgerEntity.currency()
            foundLedgers.first().balance() == ledgerEntity.balance()
            foundLedgers.first().debit() == ledgerEntity.debit()
            foundLedgers.first().credit() == ledgerEntity.credit()
        }
    }

    def "should get last ledger for day"() {
        given:
        var now = OffsetDateTime.now(ZoneOffset.UTC)
        var leaseId = UUID.randomUUID()

        when:
        [1, 2, 3].each { i ->
            ledgerRepository.insert(new LedgerEntity(leaseId, now, null, "transaction " + i, "USD", 100.0, 100.0, 100.0))
        }
        var foundLedger = ledgerRepository.findLastLedgerForDay(leaseId, now.toLocalDate())

        then:
        foundLedger.isPresent()

        expect:
        foundLedger.get().description() == "transaction 3"
    }

    def "should delete all by lease id after specified date"() {
        given:
        var now = OffsetDateTime.now(ZoneOffset.UTC)
        var leaseId = UUID.randomUUID()

        when:
        [-1, 0, 1].each { i ->
            ledgerRepository.insert(new LedgerEntity(leaseId, now.plusDays(i), null, "transaction " + i, "USD", 100.0, 100.0, 100.0))
        }
        ledgerRepository.deleteAllByLeaseIdAfter(leaseId, now.toLocalDate())

        then:
        var foundByLeaseId = ledgerRepository.findAllByLeaseId(leaseId, now.toLocalDate(), 0, 20);

        expect:
        with(foundByLeaseId) {
            foundByLeaseId.size() == 1
            foundByLeaseId.first().entryDate() == now.minusDays(1)
        }
    }
}