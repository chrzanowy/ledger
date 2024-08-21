package com.chrzanowy.model;

import static com.chrzanowy.model.TransactionType.CHARGE;
import static com.chrzanowy.model.TransactionType.CREDIT;
import static com.chrzanowy.model.TransactionType.PAYMENT;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TransactionSubType {
    CREDIT_CORRECTION(CREDIT),
    CREDIT_MARKETING(CREDIT),
    BASE_PAYMENT(PAYMENT),
    BASE_CHARGE(CHARGE);

    private final TransactionType transactionType;

    public static TransactionSubType of(CreditFeeGroup feeGroup) {
        return switch (feeGroup) {
            case CORRECTIONS -> CREDIT_CORRECTION;
            case MARKETING -> CREDIT_MARKETING;
        };
    }
}
