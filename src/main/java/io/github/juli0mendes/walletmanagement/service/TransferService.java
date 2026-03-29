package io.github.juli0mendes.walletmanagement.service;

import io.github.juli0mendes.walletmanagement.dto.TransferRequest;
import io.github.juli0mendes.walletmanagement.dto.TransferResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransferService {

    public TransferResponse transfer(TransferRequest request) {
        UUID transferId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        return new TransferResponse(
                transferId,
                request.getSourceWalletId(),
                request.getDestinationWalletId(),
                request.getAmount(),
                "COMPLETED",
                request.getDescription(),
                now
        );
    }
}

