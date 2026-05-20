package id.ac.ui.cs.advprog.auctionwallet.wallet.service;

import id.ac.ui.cs.advprog.auctionwallet.wallet.model.Wallet;
import id.ac.ui.cs.advprog.auctionwallet.wallet.model.WalletTransaction;

import java.math.BigDecimal;
import java.util.List;

/**
 * WalletService defines the contract for all wallet operations.
 *
 * Depending on this interface rather than the concrete implementation (WalletServiceImpl)
 * satisfies the Dependency Inversion Principle (D in SOLID) and makes every
 * caller (WalletController, WalletGrpcServiceImpl) easy to test by allowing
 * Mockito to replace the dependency with a mock.
 */
public interface WalletService {

    /**
     * Returns an existing wallet for the given user, or creates and persists a new one.
     * This is the only place where auto-creation occurs, and is intentionally called
     * exclusively by the gRPC CreateWallet endpoint.
     */
    Wallet getWallet(String userId);

    /**
     * Adds funds to a user's available balance and records the transaction.
     *
     * @throws id.ac.ui.cs.advprog.auctionwallet.wallet.exception.WalletNotFoundException
     *         if the user has no wallet (must call CreateWallet first)
     */
    void topUp(String userId, BigDecimal amount);

    /**
     * Deducts funds from a user's available balance.
     *
     * @throws id.ac.ui.cs.advprog.auctionwallet.wallet.exception.WalletNotFoundException
     *         if the wallet does not exist
     * @throws id.ac.ui.cs.advprog.auctionwallet.wallet.exception.InsufficientBalanceException
     *         if available balance is less than the requested amount
     */
    void withdraw(String userId, BigDecimal amount);

    /**
     * Moves funds from available to held when a bid is placed.
     *
     * @param referenceId the auction or bid ID for the audit trail
     * @throws id.ac.ui.cs.advprog.auctionwallet.wallet.exception.WalletNotFoundException
     *         if the wallet does not exist
     * @throws id.ac.ui.cs.advprog.auctionwallet.wallet.exception.InsufficientBalanceException
     *         if available balance is less than the requested amount
     */
    void holdForBid(String userId, BigDecimal amount, String referenceId);

    /**
     * Moves funds from held back to available when a user is outbid.
     *
     * @param referenceId the auction or bid ID for the audit trail
     * @throws id.ac.ui.cs.advprog.auctionwallet.wallet.exception.WalletNotFoundException
     *         if the wallet does not exist
     * @throws id.ac.ui.cs.advprog.auctionwallet.wallet.exception.InsufficientBalanceException
     *         if held balance is less than the amount to release
     */
    void releaseFromBid(String userId, BigDecimal amount, String referenceId);

    /**
     * Deducts held funds as payment when a user wins an auction.
     *
     * @param referenceId the auction or bid ID for the audit trail
     * @throws id.ac.ui.cs.advprog.auctionwallet.wallet.exception.WalletNotFoundException
     *         if the wallet does not exist
     * @throws id.ac.ui.cs.advprog.auctionwallet.wallet.exception.InsufficientBalanceException
     *         if held balance is less than the amount to settle
     */
    void payFromHeld(String userId, BigDecimal amount, String referenceId);

    /**
     * Returns the full transaction history for a user, newest first.
     */
    List<WalletTransaction> getHistory(String userId);
}