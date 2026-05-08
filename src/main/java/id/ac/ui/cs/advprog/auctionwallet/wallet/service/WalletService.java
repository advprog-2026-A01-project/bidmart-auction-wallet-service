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

@Service
public class WalletService {
    
    private static final String WALLET_NOT_FOUND = "Wallet not found";
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final WalletEventPublisher eventPublisher;

    public WalletService(WalletRepository walletRepository, 
                         WalletTransactionRepository transactionRepository,
                         WalletEventPublisher eventPublisher) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.eventPublisher = eventPublisher;
    }

    public Wallet getWallet(String userId) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> walletRepository.save(new Wallet(userId)));
    }

    @Transactional
    public void topUp(String userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseGet(() -> new Wallet(userId));
        BigDecimal balanceBefore = wallet.getAvailableBalance();
        wallet.addBalance(amount);
        walletRepository.save(wallet);
        
        transactionRepository.save(new WalletTransaction(userId, TransactionType.TOP_UP, amount, balanceBefore, wallet.getAvailableBalance(), null));
        eventPublisher.publishBalanceChangeEvent(userId, TransactionType.TOP_UP, amount, wallet.getAvailableBalance());
    }

    @Transactional
    public void withdraw(String userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new WalletNotFoundException(WALLET_NOT_FOUND));
        BigDecimal balanceBefore = wallet.getAvailableBalance();
        
        try {
            wallet.withdrawBalance(amount);
        } catch (IllegalArgumentException e) {
            throw new InsufficientBalanceException(e.getMessage(), e);
        }
        
        walletRepository.save(wallet);
        transactionRepository.save(new WalletTransaction(userId, TransactionType.WITHDRAWAL, amount, balanceBefore, wallet.getAvailableBalance(), null));
        eventPublisher.publishBalanceChangeEvent(userId, TransactionType.WITHDRAWAL, amount, wallet.getAvailableBalance());
    }

    @Transactional
    public void holdForBid(String userId, BigDecimal amount, String referenceId) {
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new WalletNotFoundException(WALLET_NOT_FOUND));
        BigDecimal balanceBefore = wallet.getAvailableBalance();
        
        try {
            wallet.holdBalance(amount);
        } catch (IllegalArgumentException e) {
            throw new InsufficientBalanceException(e.getMessage(), e);
        }
        
        walletRepository.save(wallet);
        transactionRepository.save(new WalletTransaction(userId, TransactionType.HOLD, amount, balanceBefore, wallet.getAvailableBalance(), referenceId));
        eventPublisher.publishBalanceChangeEvent(userId, TransactionType.HOLD, amount, wallet.getAvailableBalance());
    }

    @Transactional
    public void releaseFromBid(String userId, BigDecimal amount, String referenceId) {
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new WalletNotFoundException(WALLET_NOT_FOUND));
        BigDecimal balanceBefore = wallet.getAvailableBalance();
        
        try {
            wallet.releaseBalance(amount);
        } catch (IllegalArgumentException e) {
            throw new InsufficientBalanceException(e.getMessage(), e);
        }
        
        walletRepository.save(wallet);
        transactionRepository.save(new WalletTransaction(userId, TransactionType.RELEASE, amount, balanceBefore, wallet.getAvailableBalance(), referenceId));
        eventPublisher.publishBalanceChangeEvent(userId, TransactionType.RELEASE, amount, wallet.getAvailableBalance());
    }

    @Transactional
    public void payFromHeld(String userId, BigDecimal amount, String referenceId) {
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new WalletNotFoundException(WALLET_NOT_FOUND));
        BigDecimal balanceBefore = wallet.getAvailableBalance();
        
        try {
            wallet.deductHeldBalance(amount);
        } catch (IllegalArgumentException e) {
            throw new InsufficientBalanceException(e.getMessage(), e);
        }
        
        walletRepository.save(wallet);
        transactionRepository.save(new WalletTransaction(userId, TransactionType.PAYMENT, amount, balanceBefore, wallet.getAvailableBalance(), referenceId));
        eventPublisher.publishBalanceChangeEvent(userId, TransactionType.PAYMENT, amount, wallet.getAvailableBalance());
    }

    public List<WalletTransaction> getHistory(String userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}