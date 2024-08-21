package com.chrzanowy.ledger;

import com.chrzanowy.ledger.model.LedgerRecalculationEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LedgerEventListener {

    private final LedgerService ledgerService;

    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    @EventListener
    public void handle(LedgerRecalculationEvent event) {
        log.info("Handling new ledger event: {}", event);
        executorService.submit(() -> ledgerService.recalculateLedger(event.getLeaseId(), event.getLastUpdate()));
    }

}
