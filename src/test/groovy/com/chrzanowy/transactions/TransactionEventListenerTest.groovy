package com.chrzanowy.transactions


import com.chrzanowy.model.TransactionStatus
import com.chrzanowy.model.TransactionSubType
import com.chrzanowy.model.TransactionType
import com.chrzanowy.transactions.model.Transaction
import com.chrzanowy.transactions.model.TransactionEvent
import org.javamoney.moneta.FastMoney
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.time.OffsetDateTime
import java.time.ZoneOffset

class TransactionEventListenerTest extends Specification {

    private TransactionsService transactionsService = Mock(TransactionsService.class);

    def "should process new transaction on event handle"() {
        given:
        var transactionEventListener = new TransactionEventListener(transactionsService)
        var counter = 0
        var now = OffsetDateTime.now(ZoneOffset.UTC)
        var transaction = new Transaction(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                TransactionType.CREDIT, TransactionSubType.CREDIT_CORRECTION, FastMoney.of(200, "USD"),
                FastMoney.of(0, "USD"), now, TransactionStatus.SETTLED, "desc")
        transactionsService.processNewTransaction({
            tr -> (transaction == tr)
        } as Transaction) >> {
            counter++
        }

        when:
        transactionEventListener.handle(new TransactionEvent(transaction))

        then:
        new PollingConditions(timeout: 1).within(1, {
            counter == 1
        })
    }
}
