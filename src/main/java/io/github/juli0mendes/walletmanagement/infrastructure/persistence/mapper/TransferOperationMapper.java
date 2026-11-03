package io.github.juli0mendes.walletmanagement.infrastructure.persistence.mapper;

import io.github.juli0mendes.walletmanagement.domain.model.TransferOperation;
import io.github.juli0mendes.walletmanagement.domain.model.TransferOperationStatus;
import io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.TransferOperationDocument;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TransferOperationMapper {

    public TransferOperationDocument toDocument(TransferOperation transferOperation) {
        return new TransferOperationDocument(
                transferOperation.getId().toString(),
                transferOperation.getOperationId(),
                transferOperation.getTransferId() != null ? transferOperation.getTransferId().toString() : null,
                transferOperation.getSourceWalletId() != null ? transferOperation.getSourceWalletId().toString() : null,
                transferOperation.getDestinationWalletId() != null ? transferOperation.getDestinationWalletId().toString() : null,
                transferOperation.getAmount(),
                transferOperation.getPayloadHash(),
                transferOperation.getStatus().name(),
                transferOperation.getDescription(),
                transferOperation.getLeaseOwner(),
                transferOperation.getLeaseExpiresAt(),
                transferOperation.getFencingToken(),
                transferOperation.getCreatedAt(),
                transferOperation.getUpdatedAt()
        );
    }

    public TransferOperation toDomain(TransferOperationDocument document) {
        return new TransferOperation(
                UUID.fromString(document.getId()),
                document.getOperationId(),
                document.getTransferId() != null ? UUID.fromString(document.getTransferId()) : UUID.randomUUID(),
                document.getSourceWalletId() != null ? UUID.fromString(document.getSourceWalletId()) : null,
                document.getDestinationWalletId() != null ? UUID.fromString(document.getDestinationWalletId()) : null,
                document.getAmount(),
                document.getPayloadHash(),
                TransferOperationStatus.valueOf(document.getStatus()),
                document.getDescription(),
                document.getLeaseOwner(),
                document.getLeaseExpiresAt(),
                document.getFencingToken(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}
