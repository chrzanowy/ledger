package com.chrzanowy.ledger;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import com.chrzanowy.ledger.model.LedgerEntity;
import com.chrzanowy.ledger.model.LedgerEntry;
import com.chrzanowy.ledger.model.LedgerRecalculationEvent;
import com.chrzanowy.transactions.model.Transaction;
import com.chrzanowy.transactions.TransactionsService;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerService {

    private final TransactionsService transactionsService;

    private final LedgerRepository ledgerRepository;

    private final ApplicationEventMulticaster applicationEventMulticaster;

    @Transactional
    public void recalculateLedger(UUID leaseId, OffsetDateTime lastUpdate) {
        synchronized (leaseId) {
            log.info("Starting ledger recalculation for lease {} since {}", leaseId, lastUpdate);
            var transactionsPartitionedByDate = transactionsService.findAllByLeaseIdSinceExcludingCancelled(leaseId, lastUpdate)
                .stream()
                .collect(groupingBy(transaction -> transaction.date().toLocalDate(), () -> new TreeMap<>(LocalDate::compareTo), toList()));
            if (transactionsPartitionedByDate.isEmpty()) {
                return;
            }
            ledgerRepository.deleteAllByLeaseIdAfter(leaseId, lastUpdate.toLocalDate());
            transactionsPartitionedByDate.forEach((date, transactions) -> ledgerRepository.findLastLedgerForDay(leaseId, date)
                .map(LedgerEntry::from)
                .ifPresentOrElse(lastLedger -> recalculateLedgerForDay(transactions, lastLedger),
                    () -> recalculateLedgerForDay(transactions, LedgerEntry.empty(leaseId, date))));
        }
    }

    private void recalculateLedgerForDay(List<Transaction> transactions, LedgerEntry lastLedger) {
        var sortedTransactions = transactions.stream()
            .sorted(Comparator.comparingInt(LedgerTransactionOrderCalculator::calculateOrder)).toList();
        for (var transaction : sortedTransactions) {
            var ledgerTransaction = buildLedgerTransaction(transaction, lastLedger);
            ledgerRepository.insert(LedgerEntity.from(ledgerTransaction));
            lastLedger = ledgerTransaction;
        }
    }

    private LedgerEntry buildLedgerTransaction(Transaction transaction, LedgerEntry lastLedger) {
        var transactionTotalAmount = transaction.amount().add(transaction.fee()).getNumber().numberValue(BigDecimal.class);
        var currentBalance = lastLedger.balance();
        return switch (LedgerTransactionOrderCalculator.getLedgerTransactionType(transaction)) {
            case CREDIT ->
                LedgerEntry.credit(transaction.leaseId(), transaction.date(), transaction.description(), transaction.amount().getCurrency().getCurrencyCode(),
                    transactionTotalAmount, currentBalance.add(transactionTotalAmount));
            case DEBIT ->
                LedgerEntry.debit(transaction.leaseId(), transaction.date(), transaction.description(), transaction.amount().getCurrency().getCurrencyCode(),
                    transactionTotalAmount, currentBalance.subtract(transactionTotalAmount));
        };
    }

    public List<LedgerEntry> getLedgerForLease(UUID leaseId, LocalDate atDate, int offset, int max) {
        return ledgerRepository.findAllByLeaseId(leaseId, atDate, offset, max)
            .stream()
            .map(LedgerEntry::from)
            .toList();
    }

    public void sendRecalculationEvent(UUID leaseId) {
        log.info("Manual recalculation ledger requested: {}", leaseId);
        applicationEventMulticaster.multicastEvent(new LedgerRecalculationEvent(leaseId, OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC)));
    }
}
