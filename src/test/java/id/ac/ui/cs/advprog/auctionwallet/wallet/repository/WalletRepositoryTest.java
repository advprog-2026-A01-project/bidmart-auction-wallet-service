package id.ac.ui.cs.advprog.auctionwallet.wallet.repository;

import id.ac.ui.cs.advprog.auctionwallet.wallet.model.Wallet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@SuppressWarnings({"PMD.UnitTestShouldIncludeAssert", "PMD.AvoidDuplicateLiterals", "PMD.UnitTestContainsTooManyAsserts", "PMD.UnitTestAssertionsShouldIncludeMessage"})
class WalletRepositoryTest {

    @Autowired
    private WalletRepository walletRepository;

    @Test
    void testSaveAndFindByUserId() {
        Wallet wallet = new Wallet("user-123");
        wallet.addBalance(new BigDecimal("50000.00"));
        walletRepository.save(wallet);

        Optional<Wallet> foundWallet = walletRepository.findByUserId("user-123");
        
        assertTrue(foundWallet.isPresent());
        assertEquals(new BigDecimal("50000.00"), foundWallet.get().getAvailableBalance());
    }
}
