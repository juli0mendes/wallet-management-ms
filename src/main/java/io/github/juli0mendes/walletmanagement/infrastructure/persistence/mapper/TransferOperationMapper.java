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
                transferOperation.getPayloadHash(),
                transferOperation.getStatus().name(),
                transferOperation.getDescription(),
                transferOperation.getCreatedAt(),
                transferOperation.getUpdatedAt()
        );
    }

    public TransferOperation toDomain(TransferOperationDocument document) {
        return new TransferOperation(
                UUID.fromString(document.getId()),
                document.getOperationId(),
                document.getPayloadHash(),
                TransferOperationStatus.valueOf(document.getStatus()),
                document.getDescription(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}
