package id.ac.ui.cs.advprog.auctionwallet.wallet.service;

import id.ac.ui.cs.advprog.auctionwallet.wallet.exception.InsufficientBalanceException;
import id.ac.ui.cs.advprog.auctionwallet.wallet.exception.WalletNotFoundException;
import id.ac.ui.cs.advprog.auctionwallet.wallet.model.TransactionType;
import id.ac.ui.cs.advprog.auctionwallet.wallet.model.Wallet;
import id.ac.ui.cs.advprog.auctionwallet.wallet.model.WalletTransaction;
import id.ac.ui.cs.advprog.auctionwallet.wallet.repository.WalletRepository;
import id.ac.ui.cs.advprog.auctionwallet.wallet.repository.WalletTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@SuppressWarnings("PMD.LawOfDemeter")
class HoldForBidExecutor {

    private static final String WALLET_NOT_FOUND = "Wallet not found for user";

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    HoldForBidExecutor(WalletRepository walletRepository,
                       WalletTransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    BigDecimal execute(String userId, BigDecimal amount, String referenceId) {
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new WalletNotFoundException(
                        WALLET_NOT_FOUND + ": " + userId));

        BigDecimal balanceBefore = wallet.getAvailableBalance();

        try {
            wallet.holdBalance(amount);
        } catch (IllegalArgumentException e) {
            throw new InsufficientBalanceException(e.getMessage(), e);
        }

        walletRepository.save(wallet);
        transactionRepository.save(new WalletTransaction(
                userId,
                TransactionType.HOLD,
                amount,
                balanceBefore,
                wallet.getAvailableBalance(),
                referenceId));

        return wallet.getAvailableBalance();
    }
}
