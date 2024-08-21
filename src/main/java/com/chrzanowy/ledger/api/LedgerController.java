package com.chrzanowy.ledger.api;

import com.chrzanowy.ledger.model.LedgerEntry;
import com.chrzanowy.ledger.LedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ledger")
public class LedgerController {

    private final LedgerService ledgerService;

    @GetMapping("/{leaseId}")
    @Operation(summary = "Return ledger for lease ordered from last entry to first, paginated by offset and limit (max 20)")
    @ResponseStatus(HttpStatus.OK)
    public List<LedgerEntry> getLedgerForLease(@Parameter(description = "Specified lease") @PathVariable UUID leaseId,
        @Parameter(description = "Date of last ledger entry") @RequestParam(required = false) LocalDate onDate,
        @Parameter(description = "Offset for pagination") @RequestParam(required = false, defaultValue = "0") int offset,
        @Parameter(description = "Limit(max 20) for pagination") @RequestParam(required = false, defaultValue = "20") int limit) {
        return ledgerService.getLedgerForLease(leaseId, Optional.ofNullable(onDate).orElse(OffsetDateTime.now(ZoneOffset.UTC).toLocalDate()), offset,
            Math.max(1, Math.min(20, limit)));
    }

    @PutMapping("/{leaseId}/recalculate")
    @Operation(summary = "Recalculate ledger manually for provided lease")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void recalculateLedger(@Parameter(description = "Specified lease") @PathVariable UUID leaseId) {
        ledgerService.sendRecalculationEvent(leaseId);
    }
}
