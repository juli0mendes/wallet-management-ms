package io.github.juli0mendes.walletmanagement.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TransferOperationReconciliationJob {

    private static final Logger log = LoggerFactory.getLogger(TransferOperationReconciliationJob.class);

    private final TransferOperationReconciliationService reconciliationService;

    @Value("${wallet.transfer.reconciliation.enabled:true}")
    private boolean enabled;

    @Value("${wallet.transfer.reconciliation.batch-limit:20}")
    private int batchLimit;

    public TransferOperationReconciliationJob(TransferOperationReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @Scheduled(fixedDelayString = "${wallet.transfer.reconciliation.fixed-delay:30000}", initialDelayString = "${wallet.transfer.reconciliation.initial-delay:5000}")
    public void reconcileUnknownTransfers() {
        if (!enabled) {
            log.debug("Transfer operation reconciliation job is disabled");
            return;
        }

        try {
            reconciliationService.reconcileUnknownOperations(batchLimit);
        } catch (Exception ex) {
            log.error("Transfer operation reconciliation job failed batchLimit={} error={}", batchLimit, ex.getMessage(), ex);
        }
    }
}
