package io.github.juli0mendes.walletmanagement.application.usecase;

import io.github.juli0mendes.walletmanagement.domain.model.Wallet;
import io.github.juli0mendes.walletmanagement.domain.repository.WalletRepository;
import io.github.juli0mendes.walletmanagement.dto.CreateWalletRequest;
import io.github.juli0mendes.walletmanagement.dto.WalletResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class CreateWalletUseCase {

    private final WalletRepository walletRepository;

    public CreateWalletUseCase(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public WalletResponse execute(CreateWalletRequest request) {
        UUID walletId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Wallet wallet = new Wallet(
                walletId,
                request.ownerName(),
                request.ownerEmail(),
                BigDecimal.ZERO,
                now,
                now
        );

        Wallet savedWallet = walletRepository.save(wallet);

        return new WalletResponse(
                savedWallet.getId(),
                savedWallet.getOwnerName(),
                savedWallet.getOwnerEmail(),
                savedWallet.getBalance(),
                savedWallet.getCreatedAt(),
                savedWallet.getUpdatedAt()
        );
    }
}
