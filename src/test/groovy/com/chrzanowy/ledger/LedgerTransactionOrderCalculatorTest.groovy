package com.chrzanowy.ledger

import com.chrzanowy.ledger.model.LedgerTransactionType
import com.chrzanowy.model.TransactionStatus
import com.chrzanowy.model.TransactionSubType
import com.chrzanowy.model.TransactionType
import com.chrzanowy.transactions.model.Transaction
import org.javamoney.moneta.FastMoney
import spock.lang.Specification

import java.time.OffsetDateTime
import java.time.ZoneOffset

class LedgerTransactionOrderCalculatorTest extends Specification {

    def "should calculate order for each transaction"() {
        given:
        var now = OffsetDateTime.now(ZoneOffset.UTC)
        var transactionCreditCorrection = new Transaction(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                TransactionType.CREDIT, TransactionSubType.CREDIT_CORRECTION, FastMoney.of(200, "USD"),
                FastMoney.of(0, "USD"), now, TransactionStatus.SETTLED, "desc")
        var transactionCreditMarketing = new Transaction(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                TransactionType.CREDIT, TransactionSubType.CREDIT_MARKETING, FastMoney.of(200, "USD"),
                FastMoney.of(0, "USD"), now, TransactionStatus.SETTLED, "desc")
        var transactionPayment = new Transaction(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                TransactionType.PAYMENT, TransactionSubType.BASE_PAYMENT, FastMoney.of(200, "USD"),
                FastMoney.of(0, "USD"), now, TransactionStatus.SETTLED, "desc")
        var transactionCharge = new Transaction(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                TransactionType.CHARGE, TransactionSubType.BASE_CHARGE, FastMoney.of(200, "USD"),
                FastMoney.of(0, "USD"), now, TransactionStatus.SETTLED, "desc")

        when:
        var transactionCreditCorrectionOrder = LedgerTransactionOrderCalculator.calculateOrder(transactionCreditCorrection)
        var transactionCreditMarketingOrder = LedgerTransactionOrderCalculator.calculateOrder(transactionCreditMarketing)
        var transactionPaymentOrder = LedgerTransactionOrderCalculator.calculateOrder(transactionPayment)
        var transactionChargeOrder = LedgerTransactionOrderCalculator.calculateOrder(transactionCharge)

        then:
        transactionChargeOrder < transactionCreditCorrectionOrder
        transactionCreditCorrectionOrder < transactionPaymentOrder
        transactionPaymentOrder < transactionCreditMarketingOrder
    }

    def "should map transaction type to ledger transaction type"(Transaction transaction, LedgerTransactionType expectedLedgerTransactionType) {
        expect:
        LedgerTransactionOrderCalculator.getLedgerTransactionType(transaction) == expectedLedgerTransactionType

        where:
        transaction                                                                                            | expectedLedgerTransactionType
        new Transaction(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                TransactionType.CREDIT, TransactionSubType.CREDIT_CORRECTION, FastMoney.of(200, "USD"),
                FastMoney.of(0, "USD"), OffsetDateTime.now(ZoneOffset.UTC), TransactionStatus.SETTLED, "desc") | LedgerTransactionType.CREDIT
        new Transaction(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                TransactionType.CHARGE, TransactionSubType.BASE_CHARGE, FastMoney.of(200, "USD"),
                FastMoney.of(0, "USD"), OffsetDateTime.now(ZoneOffset.UTC), TransactionStatus.SETTLED, "desc") | LedgerTransactionType.DEBIT
        new Transaction(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                TransactionType.PAYMENT, TransactionSubType.BASE_PAYMENT, FastMoney.of(200, "USD"),
                FastMoney.of(0, "USD"), OffsetDateTime.now(ZoneOffset.UTC), TransactionStatus.SETTLED, "desc") | LedgerTransactionType.CREDIT

    }
}
