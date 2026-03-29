package io.github.juli0mendes.walletmanagement.controller;

import io.github.juli0mendes.walletmanagement.dto.TransferRequest;
import io.github.juli0mendes.walletmanagement.dto.TransferResponse;
import io.github.juli0mendes.walletmanagement.application.usecase.TransferUseCase;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private static final Logger log = LoggerFactory.getLogger(TransferController.class);

    private final TransferUseCase transferUseCase;

    public TransferController(TransferUseCase transferUseCase) {
        this.transferUseCase = transferUseCase;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        log.info("Transfer requested sourceWalletId={} destinationWalletId={} amount={}",
                request.sourceWalletId(), request.destinationWalletId(), request.amount());
        TransferResponse response = transferUseCase.execute(request);
        log.info("Transfer completed transferId={} sourceWalletId={} destinationWalletId={} amount={} status={}",
                response.getTransferId(), response.getSourceWalletId(), response.getDestinationWalletId(),
                response.getAmount(), response.getStatus());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
