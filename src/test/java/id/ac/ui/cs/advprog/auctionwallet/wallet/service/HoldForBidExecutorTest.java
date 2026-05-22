package id.ac.ui.cs.advprog.auctionwallet.wallet.service;

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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({
    "PMD.UnitTestShouldIncludeAssert",
    "PMD.AvoidDuplicateLiterals",
    "PMD.UnitTestContainsTooManyAsserts",
    "PMD.UnitTestAssertionsShouldIncludeMessage",
    "PMD.LawOfDemeter"
})
class HoldForBidExecutorTest {

    private static final String USER_ID = "user-123";
    private static final String REF_ID  = "auc-456";

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository transactionRepository;

    @InjectMocks
    private HoldForBidExecutor executor;

    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        testWallet = new Wallet(USER_ID);
        testWallet.addBalance(new BigDecimal("100000.00"));
    }

    @Test
    void testExecuteHoldSuccess_returnsNewBalance() {
        when(walletRepository.findByUserIdWithLock(USER_ID)).thenReturn(Optional.of(testWallet));

        BigDecimal result = executor.execute(USER_ID, new BigDecimal("30000.00"), REF_ID);

        assertEquals(new BigDecimal("70000.00"), result);
    }

    @Test
    void testExecuteHoldSuccess_updatesWalletBalances() {
        when(walletRepository.findByUserIdWithLock(USER_ID)).thenReturn(Optional.of(testWallet));

        executor.execute(USER_ID, new BigDecimal("30000.00"), REF_ID);

        assertEquals(new BigDecimal("70000.00"), testWallet.getAvailableBalance());
        assertEquals(new BigDecimal("30000.00"), testWallet.getHeldBalance());
    }

    @Test
    void testExecuteHoldSuccess_savesWallet() {
        when(walletRepository.findByUserIdWithLock(USER_ID)).thenReturn(Optional.of(testWallet));

        executor.execute(USER_ID, new BigDecimal("30000.00"), REF_ID);

        verify(walletRepository).save(testWallet);
    }

    @Test
    void testExecuteHoldSuccess_savesTransactionRecord() {
        when(walletRepository.findByUserIdWithLock(USER_ID)).thenReturn(Optional.of(testWallet));

        executor.execute(USER_ID, new BigDecimal("30000.00"), REF_ID);

        ArgumentCaptor<WalletTransaction> captor = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(transactionRepository).save(captor.capture());

        WalletTransaction saved = captor.getValue();
        assertEquals(USER_ID, saved.getUserId());
        assertEquals(TransactionType.HOLD, saved.getType());
        assertEquals(new BigDecimal("30000.00"), saved.getAmount());
        assertEquals(REF_ID, saved.getReferenceId());
    }

    @Test
    void testExecuteThrowsWalletNotFound() {
        when(walletRepository.findByUserIdWithLock("unknown")).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class,
                () -> executor.execute("unknown", new BigDecimal("100.00"), REF_ID));
    }

    @Test
    void testExecuteThrowsInsufficientBalance() {
        when(walletRepository.findByUserIdWithLock(USER_ID)).thenReturn(Optional.of(testWallet));

        assertThrows(InsufficientBalanceException.class,
                () -> executor.execute(USER_ID, new BigDecimal("999999.00"), REF_ID));
    }

    @Test
    void testExecuteThrowsInsufficientBalance_doesNotSaveWallet() {
        when(walletRepository.findByUserIdWithLock(USER_ID)).thenReturn(Optional.of(testWallet));

        assertThrows(InsufficientBalanceException.class,
                () -> executor.execute(USER_ID, new BigDecimal("999999.00"), REF_ID));

        verify(walletRepository, org.mockito.Mockito.never()).save(any());
        verify(transactionRepository, org.mockito.Mockito.never()).save(any());
    }
}