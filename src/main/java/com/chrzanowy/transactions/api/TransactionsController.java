package com.chrzanowy.transactions.api;

import com.chrzanowy.transactions.model.Transaction;
import com.chrzanowy.transactions.TransactionsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transactions")
public class TransactionsController {

    private final TransactionsService transactionsService;

    @GetMapping("/by-lease/{leaseId}")
    @Operation(summary = "Return transactions for lease ordered from last entry to first, paginated by offset and limit (max 20)")
    @ResponseStatus(HttpStatus.OK)
    public List<Transaction> getTransactions(@Parameter(description = "Specified lease") @PathVariable UUID leaseId,
        @Parameter(description = "Date of last transaction entry") @RequestParam(required = false) OffsetDateTime since,
        @Parameter(description = "Offset for pagination") @RequestParam(required = false, defaultValue = "0") int offset,
        @Parameter(description = "Limit(max 20) for pagination") @RequestParam(required = false, defaultValue = "20") int limit) {
        return transactionsService.findAllByLeaseIdSince(leaseId, Optional.ofNullable(since).orElse(OffsetDateTime.MIN), offset,
            Math.max(1, Math.min(20, limit)));
    }
}
