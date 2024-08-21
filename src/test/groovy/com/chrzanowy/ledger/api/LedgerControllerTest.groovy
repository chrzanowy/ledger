package com.chrzanowy.ledger.api

import com.chrzanowy.RestIntegrationSpecTest
import com.chrzanowy.ledger.LedgerRepository
import com.chrzanowy.ledger.model.LedgerEntry
import com.chrzanowy.model.TransactionStatus
import com.chrzanowy.model.TransactionSubType
import com.chrzanowy.model.TransactionType
import com.chrzanowy.transactions.TransactionsRepository
import com.chrzanowy.transactions.TransactionsService
import com.chrzanowy.transactions.model.Transaction
import com.chrzanowy.transactions.model.TransactionEntity
import io.restassured.common.mapper.TypeRef
import org.javamoney.moneta.FastMoney
import org.springframework.beans.factory.annotation.Autowired
import spock.util.concurrent.PollingConditions

import java.time.OffsetDateTime
import java.time.ZoneOffset

class LedgerControllerTest extends RestIntegrationSpecTest {

    @Autowired
    private TransactionsRepository transactionsRepository

    @Autowired
    private TransactionsService transactionsService

    @Autowired
    private LedgerRepository ledgerRepository

    def "should get ledger for lease"() {
        given:
        var leaseId = UUID.randomUUID()
        var transactionStartDate = OffsetDateTime.now(ZoneOffset.UTC).minusDays(7)

        when:
        prepareTransactions(leaseId, transactionStartDate)

        then: "wait for the ledger to be recalculated"
        new PollingConditions(timeout: 3).within(1, {
            ledgerRepository.findAllByLeaseId(leaseId, transactionStartDate.toLocalDate(), 0, 20).size() == 3
        })

        when:
        List<LedgerEntry> response = given()
                .when()
                .get("/api/v1/ledger/%s".formatted(leaseId))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(new TypeRef<List<LedgerEntry>>() {
                })

        then:
        response.size() == 3
        response.get(0).balance() == -2000 + 100 + 2000 //  - charge + payment + credit
        response.get(1).balance() == -2000 + 100//  - charge + payment + credit
        response.get(2).balance() == -2000 //  - charge + payment + credit
    }

    def "should recalculate ledger for lease"() {
        given:
        var leaseId = UUID.randomUUID()
        var now = OffsetDateTime.now(ZoneOffset.UTC)
        var transactionStartDate = now.minusDays(7)

        prepareTransactions(leaseId, transactionStartDate)
        transactionsRepository.insert(new TransactionEntity(UUID.randomUUID(), UUID.randomUUID(), leaseId, UUID.randomUUID(), TransactionType.CHARGE,
                TransactionSubType.BASE_CHARGE, 100.0, 0.0, "USD", transactionStartDate.plusDays(1), TransactionStatus.SETTLED, "description"))

        when:
        given()
                .when()
                .put("/api/v1/ledger/%s/recalculate".formatted(leaseId))
                .then()
                .statusCode(202)

        then: "wait for the ledger to be recalculated"
        new PollingConditions(timeout: 3).within(1, {
            ledgerRepository.findAllByLeaseId(leaseId, now.toLocalDate(), 0, 20).size() == 4
        })

        when:
        List<LedgerEntry> response = given()
                .when()
                .get("/api/v1/ledger/%s".formatted(leaseId))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(new TypeRef<List<LedgerEntry>>() {
                })

        then:
        response.size() == 4
        response.get(0).balance() == -2000 + 100 + 2000 - 100
        response.get(1).balance() == -2000 + 100 + 2000
        response.get(2).balance() == -2000 + 100
        response.get(3).balance() == -2000

    }

    def prepareTransactions(UUID leaseId, OffsetDateTime transactionStartDate) {
        transactionsService.processNewTransaction(new Transaction(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), leaseId,
                TransactionType.CHARGE, TransactionSubType.BASE_CHARGE, FastMoney.of(1900, "USD"),
                FastMoney.of(100, "USD"), transactionStartDate, TransactionStatus.SETTLED, "rent #1"))
        transactionsService.processNewTransaction(new Transaction(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), leaseId,
                TransactionType.PAYMENT, TransactionSubType.BASE_PAYMENT, FastMoney.of(1900, "USD"),
                FastMoney.of(100, "USD"), transactionStartDate, TransactionStatus.SETTLED, "payment rent #1"))
        transactionsService.processNewTransaction(new Transaction(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), leaseId,
                TransactionType.CREDIT, TransactionSubType.CREDIT_CORRECTION, FastMoney.of(0, "USD"),
                FastMoney.of(100, "USD"), transactionStartDate, TransactionStatus.CANCELLED, "rent #1 correction"))
        transactionsService.processNewTransaction(new Transaction(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), leaseId,
                TransactionType.CREDIT, TransactionSubType.CREDIT_CORRECTION, FastMoney.of(0, "USD"),
                FastMoney.of(100, "USD"), transactionStartDate, TransactionStatus.SETTLED, "rent #1 correction"))
    }

}
