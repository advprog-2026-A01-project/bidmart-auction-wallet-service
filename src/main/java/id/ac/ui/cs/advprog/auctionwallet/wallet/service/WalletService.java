package id.ac.ui.cs.advprog.auctionwallet.wallet.service;

import id.ac.ui.cs.advprog.auctionwallet.wallet.model.Wallet;
import id.ac.ui.cs.advprog.auctionwallet.wallet.model.WalletTransaction;
import id.ac.ui.cs.advprog.auctionwallet.wallet.repository.WalletRepository;
import id.ac.ui.cs.advprog.auctionwallet.wallet.repository.WalletTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletService {
    
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    public WalletService(WalletRepository walletRepository, WalletTransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    public Wallet getWallet(String userId) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> walletRepository.save(new Wallet(userId)));
    }

    @Transactional
    public void topUp(String userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseGet(() -> new Wallet(userId));
        wallet.addBalance(amount);
        walletRepository.save(wallet);
        transactionRepository.save(new WalletTransaction(userId, "TOPUP", amount));
    }

    @Transactional
    public void withdraw(String userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
        wallet.withdrawBalance(amount);
        walletRepository.save(wallet);
        transactionRepository.save(new WalletTransaction(userId, "WITHDRAW", amount));
    }

    @Transactional
    public void holdForBid(String userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
        wallet.holdBalance(amount);
        walletRepository.save(wallet);
        transactionRepository.save(new WalletTransaction(userId, "HOLD", amount));
    }

    @Transactional
    public void releaseFromBid(String userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
        wallet.releaseBalance(amount);
        walletRepository.save(wallet);
        transactionRepository.save(new WalletTransaction(userId, "RELEASE", amount));
    }

    @Transactional
    public void payFromHeld(String userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
        wallet.deductHeldBalance(amount);
        walletRepository.save(wallet);
        transactionRepository.save(new WalletTransaction(userId, "PAYMENT", amount));
    }

    public List<WalletTransaction> getHistory(String userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}