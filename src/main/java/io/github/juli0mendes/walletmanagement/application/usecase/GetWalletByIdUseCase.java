package io.github.juli0mendes.walletmanagement.application.usecase;

import io.github.juli0mendes.walletmanagement.domain.model.Wallet;
import io.github.juli0mendes.walletmanagement.domain.repository.WalletRepository;
import io.github.juli0mendes.walletmanagement.dto.WalletResponse;
import io.github.juli0mendes.walletmanagement.exception.WalletNotFoundException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GetWalletByIdUseCase {

    private final WalletRepository walletRepository;

    public GetWalletByIdUseCase(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public WalletResponse execute(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId.toString()));

        return new WalletResponse(
                wallet.getId(),
                wallet.getOwnerName(),
                wallet.getOwnerEmail(),
                wallet.getBalance(),
                wallet.getCreatedAt(),
                wallet.getUpdatedAt()
        );
    }
}
