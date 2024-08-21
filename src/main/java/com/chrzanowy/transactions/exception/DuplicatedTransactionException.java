package com.chrzanowy.transactions.exception;

import com.chrzanowy.transactions.model.TransactionEntity;

public class DuplicatedTransactionException extends RuntimeException {

    public DuplicatedTransactionException(TransactionEntity transaction) {
        super("Duplicated transaction :" + transaction);
    }

}
