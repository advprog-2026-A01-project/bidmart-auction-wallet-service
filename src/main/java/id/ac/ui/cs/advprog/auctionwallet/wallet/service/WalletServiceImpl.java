package id.ac.ui.cs.advprog.auctionwallet.wallet.service;

import id.ac.ui.cs.advprog.auctionwallet.wallet.event.WalletEventPublisher;
import id.ac.ui.cs.advprog.auctionwallet.wallet.exception.InsufficientBalanceException;
import id.ac.ui.cs.advprog.auctionwallet.wallet.exception.WalletNotFoundException;
import id.ac.ui.cs.advprog.auctionwallet.wallet.model.TransactionType;
import id.ac.ui.cs.advprog.auctionwallet.wallet.model.Wallet;
import id.ac.ui.cs.advprog.auctionwallet.wallet.model.WalletTransaction;
import id.ac.ui.cs.advprog.auctionwallet.wallet.repository.WalletRepository;
import id.ac.ui.cs.advprog.auctionwallet.wallet.repository.WalletTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * WalletServiceImpl is the primary implementation of the WalletService contract.
 *
 * All transactional wallet operations follow the same pattern:
 *   1. Acquire pessimistic write lock on the wallet row
 *   2. Validate and update the balance via domain model methods
 *   3. Persist the updated wallet
 *   4. Persist an immutable WalletTransaction audit record
 *   5. Publish a balance-changed event for downstream consumers (e.g. notification service)
 *
 * This repeated pattern is extracted into {@link #executeWalletTransaction} to keep
 * each public method concise and focused only on the operation-specific logic.
 */
@Service
public class WalletServiceImpl implements WalletService {

    private static final String WALLET_NOT_FOUND = "Wallet not found for user";

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final WalletEventPublisher eventPublisher;

    public WalletServiceImpl(WalletRepository walletRepository,
                             WalletTransactionRepository transactionRepository,
                             WalletEventPublisher eventPublisher) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.eventPublisher = eventPublisher;
    }

    // ─── Query ───────────────────────────────────────────────────────────────

    @Override
    public Wallet getWallet(String userId) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> walletRepository.save(new Wallet(userId)));
    }

    @Override
    public List<WalletTransaction> getHistory(String userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // ─── Commands ────────────────────────────────────────────────────────────

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
    @Transactional
    public void holdForBid(String userId, BigDecimal amount, String referenceId) {
        executeWalletTransaction(userId, amount, referenceId, TransactionType.HOLD,
                wallet -> wallet.holdBalance(amount));
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

    // ─── Template helper ─────────────────────────────────────────────────────

    /**
     * Executes any wallet balance mutation as a single atomic unit:
     * acquires a pessimistic write lock, applies the balance operation via the
     * provided {@code WalletOperation} strategy, persists both the updated wallet
     * and an audit transaction record, then publishes an event.
     *
     * <p>Any {@link IllegalArgumentException} thrown by the domain model is
     * re-thrown as an {@link InsufficientBalanceException} so callers receive a
     * consistent exception type regardless of which balance method was called.</p>
     *
     * @param userId       the owner of the wallet
     * @param amount       the monetary amount involved in the operation
     * @param referenceId  optional auction/bid reference for the audit record
     * @param type         the transaction type for categorisation
     * @param operation    the balance mutation to apply to the {@link Wallet}
     */
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

    /**
     * Functional interface for a single balance mutation on a {@link Wallet}.
     * Implemented as a lambda at each call site to keep each operation concise.
     */
    @FunctionalInterface
    interface WalletOperation {
        void apply(Wallet wallet);
    }
}
