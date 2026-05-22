package id.ac.ui.cs.advprog.auctionwallet.wallet.service;

import id.ac.ui.cs.advprog.auctionwallet.wallet.event.WalletEventPublisher;
import id.ac.ui.cs.advprog.auctionwallet.wallet.exception.InsufficientBalanceException;
import id.ac.ui.cs.advprog.auctionwallet.wallet.exception.WalletNotFoundException;
import id.ac.ui.cs.advprog.auctionwallet.wallet.model.TransactionType;
import id.ac.ui.cs.advprog.auctionwallet.wallet.model.Wallet;
import id.ac.ui.cs.advprog.auctionwallet.wallet.model.WalletTransaction;
import id.ac.ui.cs.advprog.auctionwallet.wallet.repository.WalletRepository;
import id.ac.ui.cs.advprog.auctionwallet.wallet.repository.WalletTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class WalletServiceImpl implements WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletServiceImpl.class);
    private static final String WALLET_NOT_FOUND = "Wallet not found for user";

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final WalletEventPublisher eventPublisher;
    private final HoldForBidExecutor holdForBidExecutor;

    public WalletServiceImpl(WalletRepository walletRepository,
                             WalletTransactionRepository transactionRepository,
                             WalletEventPublisher eventPublisher,
                             HoldForBidExecutor holdForBidExecutor) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.eventPublisher = eventPublisher;
        this.holdForBidExecutor = holdForBidExecutor;
    }

    @Override
    public Wallet getWallet(String userId) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> walletRepository.save(new Wallet(userId)));
    }

    @Override
    public List<WalletTransaction> getHistory(String userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional
    public void topUp(String userId, BigDecimal amount) {
        executeWalletTransaction(userId, amount, null, TransactionType.TOP_UP,
                wallet -> wallet.addBalance(amount));
    }

    @Override
    @Transactional
    public void withdraw(String userId, BigDecimal amount) {
        executeWalletTransaction(userId, amount, null, TransactionType.WITHDRAWAL,
                wallet -> wallet.withdrawBalance(amount));
    }

    @Override
    public void holdForBid(String userId, BigDecimal amount, String referenceId) {
        long startNano = System.nanoTime();
        BigDecimal newBalance = holdForBidExecutor.execute(userId, amount, referenceId);
        eventPublisher.publishBalanceChangeEvent(
                userId, TransactionType.HOLD, amount, newBalance);

        if (log.isInfoEnabled()) {
            log.info("holdForBid completed for user={} referenceId={} in {} ms",
                    userId, referenceId,
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNano));
        }
    }

    @Override
    @Transactional
    public void releaseFromBid(String userId, BigDecimal amount, String referenceId) {
        executeWalletTransaction(userId, amount, referenceId, TransactionType.RELEASE,
                wallet -> wallet.releaseBalance(amount));
    }

    @Override
    @Transactional
    public void payFromHeld(String userId, BigDecimal amount, String referenceId) {
        executeWalletTransaction(userId, amount, referenceId, TransactionType.PAYMENT,
                wallet -> wallet.deductHeldBalance(amount));
    }

    @SuppressWarnings("PMD.LawOfDemeter")
    private void executeWalletTransaction(String userId,
                                          BigDecimal amount,
                                          String referenceId,
                                          TransactionType type,
                                          WalletOperation operation) {
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new WalletNotFoundException(WALLET_NOT_FOUND + ": " + userId));

        BigDecimal balanceBefore = wallet.getAvailableBalance();

        try {
            operation.apply(wallet);
        } catch (IllegalArgumentException e) {
            throw new InsufficientBalanceException(e.getMessage(), e);
        }

        walletRepository.save(wallet);
        transactionRepository.save(new WalletTransaction(
                userId, type, amount, balanceBefore, wallet.getAvailableBalance(), referenceId));
        eventPublisher.publishBalanceChangeEvent(
                userId, type, amount, wallet.getAvailableBalance());
    }

    @FunctionalInterface
    interface WalletOperation {
        void apply(Wallet wallet);
    }
}
