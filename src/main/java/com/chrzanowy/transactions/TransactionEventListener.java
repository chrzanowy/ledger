package com.chrzanowy.transactions;

import com.chrzanowy.transactions.model.TransactionEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventListener {

    private final TransactionsService transactionsService;

    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    @EventListener
    public void handle(TransactionEvent event) {
        log.info("Handling new transaction event: {}", event);
        executorService.submit(() -> {
            transactionsService.processNewTransaction(event.getTransaction());
        });
    }

}
