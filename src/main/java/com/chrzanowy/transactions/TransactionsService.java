package com.chrzanowy.transactions;

import com.chrzanowy.ledger.model.LedgerRecalculationEvent;
import com.chrzanowy.model.TransactionStatus;
import com.chrzanowy.schedule.model.LedgerScheduledRecalculationEvent;
import com.chrzanowy.transactions.model.Transaction;
import com.chrzanowy.transactions.model.TransactionEntity;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionsService {

    private final TransactionsRepository transactionsRepository;

    private final ApplicationEventMulticaster applicationEventMulticaster;

    public void processNewTransaction(Transaction transaction) {
        boolean validTransaction = transactionsRepository.insert(TransactionEntity.fromTransaction(transaction));
        if (validTransaction) {
            notifyLedgerAboutNewTransaction(transaction);
        }
    }

    public List<Transaction> findAllByLeaseIdSince(UUID leaseId, OffsetDateTime since, int offset, int limit) {
        return transactionsRepository.findAllByLeaseId(leaseId, since, offset, limit)
            .stream()
            .map(Transaction::fromEntity)
            .toList();
    }

    public List<Transaction> findAllByLeaseIdSinceExcludingCancelled(UUID leaseId, OffsetDateTime since) {
        return transactionsRepository.findAllByLeaseIdSinceExcludingCancelled(leaseId, since)
            .stream()
            .map(Transaction::fromEntity)
            .toList();
    }

    private void notifyLedgerAboutNewTransaction(Transaction transaction) {
        if (TransactionStatus.SETTLED == transaction.status()) {
            if (OffsetDateTime.now(ZoneOffset.UTC).toLocalDate().isBefore(transaction.date().toLocalDate())) {
                log.info("Transaction is in the future, skipping ledger recalculation: {} for now, scheduling it for the future", transaction);
                applicationEventMulticaster.multicastEvent(new LedgerScheduledRecalculationEvent(transaction.leaseId(), transaction.date()));
                return;
            }
            log.info("Sending ledger recalculation event: {}", transaction);
            applicationEventMulticaster.multicastEvent(new LedgerRecalculationEvent(transaction.leaseId(), transaction.date()));
        }
    }
}
