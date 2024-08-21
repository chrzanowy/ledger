package com.chrzanowy.transactions

import com.chrzanowy.BaseIntegrationSpec
import com.chrzanowy.model.TransactionStatus
import com.chrzanowy.model.TransactionSubType
import com.chrzanowy.model.TransactionType
import com.chrzanowy.transactions.model.TransactionEntity
import org.springframework.beans.factory.annotation.Autowired

import java.time.OffsetDateTime
import java.time.ZoneOffset

class TransactionsRepositoryTest extends BaseIntegrationSpec {

    @Autowired
    private TransactionsRepository transactionsRepository

    def "should insert transaction and find it afterwards"() {
        given:
        var leaseId = UUID.randomUUID()
        var now = OffsetDateTime.now(ZoneOffset.UTC)
        var transaction = new TransactionEntity(UUID.randomUUID(), UUID.randomUUID(), leaseId, UUID.randomUUID(), TransactionType.CHARGE,
                TransactionSubType.BASE_CHARGE, 100.0, 0.0, "USD", now, TransactionStatus.CANCELLED, "description")

        when:
        transactionsRepository.insert(transaction)
        var foundByLeaseId = transactionsRepository.findAllByLeaseId(leaseId, now, 0, 20)
        var foundByLeaseIdExcludingCancelled = transactionsRepository.findAllByLeaseIdSinceExcludingCancelled(leaseId, now);

        then:
        foundByLeaseIdExcludingCancelled.isEmpty()

        expect:
        with(foundByLeaseId) {
            size() == 1
            first().leaseId() == leaseId
        }
    }

    def "should not insert transaction for the same even source twice"() {
        given:
        var leaseId = UUID.randomUUID()
        var eventId = UUID.randomUUID()
        var now = OffsetDateTime.now(ZoneOffset.UTC)
        var transaction = new TransactionEntity(UUID.randomUUID(), UUID.randomUUID(), leaseId, eventId, TransactionType.CHARGE,
                TransactionSubType.BASE_CHARGE, 100.0, 0.0, "USD", now, TransactionStatus.CANCELLED, "description")

        when:
        [1, 2, 3].each {
            transactionsRepository.insert(transaction)
        }
        var foundByLeaseId = transactionsRepository.findAllByLeaseId(leaseId, now, 0, 20)

        then:
        with(foundByLeaseId) {
            size() == 1
            first().eventId() == eventId
        }
    }
}
