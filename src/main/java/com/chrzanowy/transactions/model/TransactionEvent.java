package com.chrzanowy.transactions.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@Getter
@EqualsAndHashCode(callSuper = false)
@ToString
public class TransactionEvent extends ApplicationEvent {

    private final Transaction transaction;

    public TransactionEvent(Transaction transaction) {
        super("internal");
        this.transaction = transaction;
    }
}
