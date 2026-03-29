package io.github.juli0mendes.walletmanagement;

import io.github.juli0mendes.walletmanagement.infrastructure.persistence.repository.TransactionDataRepository;
import io.github.juli0mendes.walletmanagement.infrastructure.persistence.repository.WalletDataRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@DisplayName("WalletManagementMsApplication Tests")
class WalletManagementMsApplicationTest {

    @MockitoBean
    WalletDataRepository walletDataRepository;

    @MockitoBean
    TransactionDataRepository transactionDataRepository;

    @Test
    @DisplayName("Should load application context successfully")
    void contextLoads() {}
}
