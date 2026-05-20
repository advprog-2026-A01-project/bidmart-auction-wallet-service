package id.ac.ui.cs.advprog.auctionwallet.wallet.service;

import id.ac.ui.cs.advprog.auctionwallet.wallet.event.WalletEventPublisher;
import id.ac.ui.cs.advprog.auctionwallet.wallet.exception.InsufficientBalanceException;
import id.ac.ui.cs.advprog.auctionwallet.wallet.exception.WalletNotFoundException;
import id.ac.ui.cs.advprog.auctionwallet.wallet.model.TransactionType;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({
    "PMD.UnitTestShouldIncludeAssert",
    "PMD.AvoidDuplicateLiterals",
    "PMD.UnitTestContainsTooManyAsserts",
    "PMD.UnitTestAssertionsShouldIncludeMessage",
    "PMD.TooManyMethods"
})
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository transactionRepository;

    @Mock
    private WalletEventPublisher eventPublisher;

    @InjectMocks
    private WalletServiceImpl walletService;

    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        testWallet = new Wallet("user-123");
        testWallet.addBalance(new BigDecimal("100000.00"));
    }

    // ─── getWallet ───────────────────────────────────────────────────────────

    @Test
    void testGetWalletReturnsExistingWallet() {
        when(walletRepository.findByUserId("user-123")).thenReturn(Optional.of(testWallet));

        Wallet result = walletService.getWallet("user-123");

        assertEquals(testWallet, result);
        verify(walletRepository, never()).save(any());
    }

    @Test
    void testGetWalletCreatesNewWalletIfNotFound() {
        when(walletRepository.findByUserId("new-user")).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));

        Wallet result = walletService.getWallet("new-user");

        assertNotNull(result);
        assertEquals("new-user", result.getUserId());
        verify(walletRepository).save(any(Wallet.class));
    }

    // ─── topUp ───────────────────────────────────────────────────────────────

    @Test
    void testTopUpSuccess() {
        when(walletRepository.findByUserIdWithLock("user-123")).thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

        walletService.topUp("user-123", new BigDecimal("50000.00"));

        assertEquals(new BigDecimal("150000.00"), testWallet.getAvailableBalance());
        verify(walletRepository).save(testWallet);
        verify(transactionRepository).save(any(WalletTransaction.class));
        verify(eventPublisher).publishBalanceChangeEvent(any(), any(), any(), any());
    }

    @Test
    void testTopUpThrowsWhenWalletNotFound() {
        when(walletRepository.findByUserIdWithLock("unknown")).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class,
                () -> walletService.topUp("unknown", new BigDecimal("1000.00")));
    }

    // ─── withdraw ────────────────────────────────────────────────────────────

    @Test
    void testWithdrawSuccess() {
        when(walletRepository.findByUserIdWithLock("user-123")).thenReturn(Optional.of(testWallet));

        walletService.withdraw("user-123", new BigDecimal("40000.00"));

        assertEquals(new BigDecimal("60000.00"), testWallet.getAvailableBalance());
        verify(walletRepository).save(testWallet);
        verify(eventPublisher).publishBalanceChangeEvent(any(), any(), any(), any());
    }

    @Test
    void testWithdrawThrowsWhenWalletNotFound() {
        when(walletRepository.findByUserIdWithLock("unknown")).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class,
                () -> walletService.withdraw("unknown", new BigDecimal("100.00")));
    }

    @Test
    void testWithdrawThrowsWhenInsufficientBalance() {
        when(walletRepository.findByUserIdWithLock("user-123")).thenReturn(Optional.of(testWallet));

        assertThrows(InsufficientBalanceException.class,
                () -> walletService.withdraw("user-123", new BigDecimal("999999.00")));
    }

    // ─── holdForBid ──────────────────────────────────────────────────────────

    @Test
    void testHoldForBidSuccess() {
        when(walletRepository.findByUserIdWithLock("user-123")).thenReturn(Optional.of(testWallet));

        walletService.holdForBid("user-123", new BigDecimal("30000.00"), "auc-123");

        assertEquals(new BigDecimal("70000.00"), testWallet.getAvailableBalance());
        assertEquals(new BigDecimal("30000.00"), testWallet.getHeldBalance());
    }

    @Test
    void testHoldForBidThrowsWhenWalletNotFound() {
        when(walletRepository.findByUserIdWithLock("unknown")).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class,
                () -> walletService.holdForBid("unknown", new BigDecimal("100.00"), "auc-123"));
    }

    @Test
    void testHoldForBidThrowsWhenInsufficientBalance() {
        when(walletRepository.findByUserIdWithLock("user-123")).thenReturn(Optional.of(testWallet));

        assertThrows(InsufficientBalanceException.class,
                () -> walletService.holdForBid("user-123", new BigDecimal("999999.00"), "auc-123"));
    }

    // ─── releaseFromBid ──────────────────────────────────────────────────────

    @Test
    void testReleaseFromBidSuccess() {
        testWallet.holdBalance(new BigDecimal("30000.00"));
        when(walletRepository.findByUserIdWithLock("user-123")).thenReturn(Optional.of(testWallet));

        walletService.releaseFromBid("user-123", new BigDecimal("30000.00"), "auc-123");

        assertEquals(new BigDecimal("100000.00"), testWallet.getAvailableBalance());
        assertEquals(BigDecimal.ZERO, testWallet.getHeldBalance());
    }

    @Test
    void testReleaseFromBidThrowsWhenWalletNotFound() {
        when(walletRepository.findByUserIdWithLock("unknown")).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class,
                () -> walletService.releaseFromBid("unknown", new BigDecimal("100.00"), "auc-123"));
    }

    @Test
    void testReleaseFromBidThrowsWhenInsufficientHeld() {
        when(walletRepository.findByUserIdWithLock("user-123")).thenReturn(Optional.of(testWallet));

        assertThrows(InsufficientBalanceException.class,
                () -> walletService.releaseFromBid("user-123", new BigDecimal("50000.00"), "auc-123"));
    }

    // ─── payFromHeld ─────────────────────────────────────────────────────────

    @Test
    void testPayFromHeldSuccess() {
        testWallet.holdBalance(new BigDecimal("50000.00"));
        when(walletRepository.findByUserIdWithLock("user-123")).thenReturn(Optional.of(testWallet));

        walletService.payFromHeld("user-123", new BigDecimal("50000.00"), "auc-123");

        assertEquals(BigDecimal.ZERO, testWallet.getHeldBalance());
        verify(transactionRepository).save(any(WalletTransaction.class));
        verify(eventPublisher).publishBalanceChangeEvent(any(), any(), any(), any());
    }

    @Test
    void testPayFromHeldThrowsWhenWalletNotFound() {
        when(walletRepository.findByUserIdWithLock("unknown")).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class,
                () -> walletService.payFromHeld("unknown", new BigDecimal("100.00"), "auc-123"));
    }

    @Test
    void testPayFromHeldThrowsWhenInsufficientHeld() {
        when(walletRepository.findByUserIdWithLock("user-123")).thenReturn(Optional.of(testWallet));

        assertThrows(InsufficientBalanceException.class,
                () -> walletService.payFromHeld("user-123", new BigDecimal("50000.00"), "auc-123"));
    }

    // ─── getHistory ──────────────────────────────────────────────────────────

    @Test
    void testGetHistoryReturnsTransactions() {
        List<WalletTransaction> txList = List.of(
                new WalletTransaction("user-123", TransactionType.TOP_UP,
                        new BigDecimal("50000"), BigDecimal.ZERO,
                        new BigDecimal("50000"), null));
        when(transactionRepository.findByUserIdOrderByCreatedAtDesc("user-123")).thenReturn(txList);

        List<WalletTransaction> result = walletService.getHistory("user-123");

        assertEquals(1, result.size());
        verify(transactionRepository).findByUserIdOrderByCreatedAtDesc("user-123");
    }

    @Test
    void testGetHistoryReturnsEmptyListWhenNoTransactions() {
        when(transactionRepository.findByUserIdOrderByCreatedAtDesc("user-123"))
                .thenReturn(List.of());

        List<WalletTransaction> result = walletService.getHistory("user-123");

        assertEquals(0, result.size());
    }
}

