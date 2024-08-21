package com.chrzanowy.ledger;

import com.chrzanowy.ledger.model.LedgerTransactionType;
import com.chrzanowy.transactions.model.Transaction;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LedgerTransactionOrderCalculator {

    private static final int CHARGE_ORDER = 1000;

    private static final int PAYMENTS_ORDER = 2000;

    public int calculateOrder(Transaction transaction) {
        return switch (transaction.type()) {
            case PAYMENT -> PAYMENTS_ORDER;
            case CHARGE -> CHARGE_ORDER;
            case CREDIT -> switch (transaction.subType()) {
                case CREDIT_CORRECTION -> CHARGE_ORDER + 1;
                case CREDIT_MARKETING -> PAYMENTS_ORDER + 1;
                default -> -1;
            };
        };
    }

    public LedgerTransactionType getLedgerTransactionType(Transaction transaction) {
        return switch (transaction.type()) {
            case PAYMENT, CREDIT -> LedgerTransactionType.CREDIT;
            case CHARGE -> LedgerTransactionType.DEBIT;
        };
    }

}
