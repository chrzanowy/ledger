package com.chrzanowy.transactions.api

import com.chrzanowy.RestIntegrationSpecTest
import com.chrzanowy.model.TransactionStatus
import com.chrzanowy.model.TransactionSubType
import com.chrzanowy.model.TransactionType
import com.chrzanowy.transactions.TransactionsRepository
import com.chrzanowy.transactions.model.Transaction
import com.chrzanowy.transactions.model.TransactionEntity
import io.restassured.common.mapper.TypeRef
import org.springframework.beans.factory.annotation.Autowired

import java.time.OffsetDateTime
import java.time.ZoneOffset

class TransactionsControllerTest extends RestIntegrationSpecTest {

    @Autowired
    private TransactionsRepository transactionsRepository;

    def "should return lease transactions ordered by time descending"() {
        given:
        var leaseId = UUID.randomUUID()
        var now = OffsetDateTime.now(ZoneOffset.UTC)
        var transaction = new TransactionEntity(UUID.randomUUID(), UUID.randomUUID(), leaseId, UUID.randomUUID(), TransactionType.CHARGE,
                TransactionSubType.BASE_CHARGE, 100.0, 0.0, "USD", now.minusDays(1), TransactionStatus.CANCELLED, "description")
        var transaction2 = new TransactionEntity(UUID.randomUUID(), UUID.randomUUID(), leaseId, UUID.randomUUID(), TransactionType.PAYMENT,
                TransactionSubType.BASE_PAYMENT, 100.0, 0.0, "USD", now, TransactionStatus.SETTLED, "description")
        transactionsRepository.insert(transaction)
        transactionsRepository.insert(transaction2)

        when:
        List<Transaction> response = given()
                .when()
                .get("/api/v1/transactions/by-lease/%s".formatted(leaseId))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(new TypeRef<List<Transaction>>() {
                })

        then:
        response.size() == 2
        response.get(0).eventId() == transaction2.eventId()
        response.get(1).eventId() == transaction.eventId()
    }


}
