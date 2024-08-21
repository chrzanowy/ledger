package com.chrzanowy.model;

public enum TransactionStatus {
    SETTLED, CANCELLED;

    public static TransactionStatus of(PaymentStatus status) {
        return switch (status) {
            case SUCCESS -> SETTLED;
            case FAILURE -> CANCELLED;
        };
    }
}
