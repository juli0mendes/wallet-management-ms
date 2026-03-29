package io.github.juli0mendes.walletmanagement.application.usecase;

import io.github.juli0mendes.walletmanagement.domain.model.Wallet;
import io.github.juli0mendes.walletmanagement.domain.repository.WalletRepository;
import io.github.juli0mendes.walletmanagement.dto.CreateWalletRequest;
import io.github.juli0mendes.walletmanagement.dto.WalletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateWalletUseCase Unit Tests")
class CreateWalletUseCaseTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private CreateWalletUseCase createWalletUseCase;

    @Test
    @DisplayName("Should create wallet successfully")
    void shouldCreateWalletSuccessfully() {
        var request = new CreateWalletRequest("John Doe", "john@example.com");
        var walletId = UUID.randomUUID();
        var now = LocalDateTime.now();
        var savedWallet = new Wallet(walletId, "John Doe", "john@example.com", BigDecimal.ZERO, now, now);

        when(walletRepository.save(any(Wallet.class))).thenReturn(savedWallet);

        WalletResponse response = createWalletUseCase.execute(request);

        assertNotNull(response);
        assertEquals(walletId, response.getWalletId());
        assertEquals("John Doe", response.getOwnerName());
        assertEquals("john@example.com", response.getOwnerEmail());
        assertEquals(BigDecimal.ZERO, response.getCurrentBalance());
        assertEquals(now, response.getCreatedAt());
        assertEquals(now, response.getUpdatedAt());
    }
}
