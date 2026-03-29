package io.github.juli0mendes.walletmanagement.application.usecase;

import io.github.juli0mendes.walletmanagement.domain.model.Wallet;
import io.github.juli0mendes.walletmanagement.domain.repository.WalletRepository;
import io.github.juli0mendes.walletmanagement.dto.WalletResponse;
import io.github.juli0mendes.walletmanagement.exception.WalletNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetWalletByIdUseCase Unit Tests")
class GetWalletByIdUseCaseTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private GetWalletByIdUseCase getWalletByIdUseCase;

    @Test
    @DisplayName("Should get wallet successfully")
    void shouldGetWalletSuccessfully() {
        var walletId = UUID.randomUUID();
        var now = LocalDateTime.now();
        var wallet = new Wallet(walletId, "John Doe", "john@example.com", BigDecimal.valueOf(100.00), now, now);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        WalletResponse response = getWalletByIdUseCase.execute(walletId);

        assertNotNull(response);
        assertEquals(walletId, response.getWalletId());
        assertEquals("John Doe", response.getOwnerName());
        assertEquals("john@example.com", response.getOwnerEmail());
        assertEquals(BigDecimal.valueOf(100.00), response.getCurrentBalance());
    }

    @Test
    @DisplayName("Should throw WalletNotFoundException when wallet not found")
    void shouldThrowWalletNotFoundExceptionWhenWalletNotFound() {
        var walletId = UUID.randomUUID();

        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> getWalletByIdUseCase.execute(walletId));
    }
}
