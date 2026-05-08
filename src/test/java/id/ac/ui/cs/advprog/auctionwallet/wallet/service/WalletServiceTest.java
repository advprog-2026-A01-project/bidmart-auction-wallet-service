package id.ac.ui.cs.advprog.auctionwallet.wallet.service;

import id.ac.ui.cs.advprog.auctionwallet.wallet.model.Wallet;
import id.ac.ui.cs.advprog.auctionwallet.wallet.model.WalletTransaction;
import id.ac.ui.cs.advprog.auctionwallet.wallet.repository.WalletRepository;
import id.ac.ui.cs.advprog.auctionwallet.wallet.repository.WalletTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository transactionRepository;

    @InjectMocks
    private WalletService walletService;

    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        testWallet = new Wallet("user-123");
        testWallet.addBalance(new BigDecimal("100000.00"));
    }

    @Test
    void testTopUp() {
        when(walletRepository.findByUserIdWithLock("user-123")).thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

        walletService.topUp("user-123", new BigDecimal("50000.00"));

        assertEquals(new BigDecimal("150000.00"), testWallet.getAvailableBalance());
        verify(walletRepository, times(1)).save(testWallet);
        verify(transactionRepository, times(1)).save(any(WalletTransaction.class));
    }

    @Test
    void testWithdrawSuccess() {
        when(walletRepository.findByUserIdWithLock("user-123")).thenReturn(Optional.of(testWallet));
        
        walletService.withdraw("user-123", new BigDecimal("40000.00"));

        assertEquals(new BigDecimal("60000.00"), testWallet.getAvailableBalance());
        verify(walletRepository, times(1)).save(testWallet);
    }

    @Test
    void testHoldForBidSuccess() {
        when(walletRepository.findByUserIdWithLock("user-123")).thenReturn(Optional.of(testWallet));

        walletService.holdForBid("user-123", new BigDecimal("30000.00"));

        assertEquals(new BigDecimal("70000.00"), testWallet.getAvailableBalance());
        assertEquals(new BigDecimal("30000.00"), testWallet.getHeldBalance());
    }

    @Test
    void testHoldForBidThrowsExceptionWhenNotFound() {
        when(walletRepository.findByUserIdWithLock("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            walletService.holdForBid("unknown", new BigDecimal("100.00"));
        });
    }
}