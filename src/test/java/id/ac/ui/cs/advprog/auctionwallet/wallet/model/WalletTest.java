package id.ac.ui.cs.advprog.auctionwallet.wallet.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WalletTest {

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = new Wallet("user-123");
    }

    @Test
    void testWalletInitialization() {
        assertEquals("user-123", wallet.getUserId());
        assertEquals(BigDecimal.ZERO, wallet.getAvailableBalance());
        assertEquals(BigDecimal.ZERO, wallet.getHeldBalance());
    }

    @Test
    void testAddBalanceSuccess() {
        wallet.addBalance(new BigDecimal("150000.00"));
        assertEquals(new BigDecimal("150000.00"), wallet.getAvailableBalance());
    }

    @Test
    void testAddBalanceNegativeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            wallet.addBalance(new BigDecimal("-100.00"));
        });
    }

    @Test
    void testHoldBalanceSuccess() {
        wallet.addBalance(new BigDecimal("100000.00"));
        wallet.holdBalance(new BigDecimal("40000.00"));
        
        assertEquals(new BigDecimal("60000.00"), wallet.getAvailableBalance());
        assertEquals(new BigDecimal("40000.00"), wallet.getHeldBalance());
    }

    @Test
    void testHoldBalanceInsufficientFunds() {
        wallet.addBalance(new BigDecimal("50000.00"));
        
        assertThrows(IllegalArgumentException.class, () -> {
            wallet.holdBalance(new BigDecimal("60000.00"));
        });
    }

    @Test
    void testReleaseBalanceSuccess() {
        wallet.addBalance(new BigDecimal("100000.00"));
        wallet.holdBalance(new BigDecimal("50000.00"));
        wallet.releaseBalance(new BigDecimal("20000.00"));

        assertEquals(new BigDecimal("70000.00"), wallet.getAvailableBalance());
        assertEquals(new BigDecimal("30000.00"), wallet.getHeldBalance());
    }

    @Test
    void testDeductHeldBalanceSuccess() {
        wallet.addBalance(new BigDecimal("100000.00"));
        wallet.holdBalance(new BigDecimal("50000.00"));
        wallet.deductHeldBalance(new BigDecimal("50000.00"));

        assertEquals(new BigDecimal("50000.00"), wallet.getAvailableBalance());
        assertEquals(new BigDecimal("0.00"), wallet.getHeldBalance());
    }
}