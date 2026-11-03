package io.github.juli0mendes.walletmanagement.controller;

import io.github.juli0mendes.walletmanagement.application.usecase.TransferUseCase;
import io.github.juli0mendes.walletmanagement.domain.exception.TransferOperationNotFoundException;
import io.github.juli0mendes.walletmanagement.domain.repository.TransferOperationRepository;
import io.github.juli0mendes.walletmanagement.dto.TransferOperationResponse;
import io.github.juli0mendes.walletmanagement.dto.TransferRequest;
import io.github.juli0mendes.walletmanagement.dto.TransferResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private static final Logger log = LoggerFactory.getLogger(TransferController.class);

    private final TransferUseCase transferUseCase;
    private final TransferOperationRepository transferOperationRepository;

    public TransferController(TransferUseCase transferUseCase,
                             TransferOperationRepository transferOperationRepository) {
        this.transferUseCase = transferUseCase;
        this.transferOperationRepository = transferOperationRepository;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        log.info("Transfer requested sourceWalletId={} destinationWalletId={} amount={} operationId={}",
                request.sourceWalletId(), request.destinationWalletId(), request.amount(), request.operationId());
        TransferResponse response = transferUseCase.execute(request);
        log.info("Transfer completed transferId={} operationId={} sourceWalletId={} destinationWalletId={} amount={} status={}",
                response.getTransferId(), response.getOperationId(), response.getSourceWalletId(), response.getDestinationWalletId(),
                response.getAmount(), response.getStatus());

        HttpStatus httpStatus = switch (response.getStatus()) {
            case "UNKNOWN_COMMIT_RESULT", "RECONCILING" -> HttpStatus.ACCEPTED;
            default -> HttpStatus.CREATED;
        };

        return new ResponseEntity<>(response, httpStatus);
    }

    @GetMapping("/operations/{operationId}")
    public ResponseEntity<TransferOperationResponse> getTransferOperation(@PathVariable String operationId) {
        log.debug("Fetching transfer operation operationId={}", operationId);
        TransferOperationResponse response = transferOperationRepository.findByOperationId(operationId)
                .map(TransferOperationResponse::fromDomain)
                .orElseThrow(() -> new TransferOperationNotFoundException(operationId));
        return ResponseEntity.ok(response);
    }
}
