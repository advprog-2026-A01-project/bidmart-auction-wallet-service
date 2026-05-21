package id.ac.ui.cs.advprog.auctionwallet.wallet.service;

import id.ac.ui.cs.advprog.auctionwallet.wallet.model.Wallet;
import id.ac.ui.cs.advprog.auctionwallet.wallet.model.WalletTransaction;

import java.math.BigDecimal;
import java.util.List;

public interface WalletService {
    Wallet getWallet(String userId);
    void topUp(String userId, BigDecimal amount);
    void withdraw(String userId, BigDecimal amount);
    void holdForBid(String userId, BigDecimal amount, String referenceId);
    void releaseFromBid(String userId, BigDecimal amount, String referenceId);
    void payFromHeld(String userId, BigDecimal amount, String referenceId);
    List<WalletTransaction> getHistory(String userId);
}