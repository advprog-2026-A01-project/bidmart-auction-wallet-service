package id.ac.ui.cs.advprog.auctionwallet.wallet.controller;

import id.ac.ui.cs.advprog.auctionwallet.wallet.model.Wallet;
import id.ac.ui.cs.advprog.auctionwallet.wallet.model.WalletTransaction;
import id.ac.ui.cs.advprog.auctionwallet.wallet.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private static final String AMOUNT_KEY = "amount";
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/me/info")
    public ResponseEntity<Map<String, BigDecimal>> getMyWalletInfo(@RequestHeader("X-User-Id") String userId) {
        Wallet wallet = walletService.getWallet(userId);
        return ResponseEntity.ok(Map.of(
                "availableBalance", wallet.getAvailableBalance(),
                "heldBalance", wallet.getHeldBalance()
        ));
    }

    @PostMapping("/me/topup")
    public ResponseEntity<String> myTopUp(@RequestHeader("X-User-Id") String userId, @RequestBody Map<String, BigDecimal> payload) {
        walletService.topUp(userId, payload.get(AMOUNT_KEY));
        return ResponseEntity.ok("Top-up successful");
    }

    @PostMapping("/me/withdraw")
    public ResponseEntity<String> myWithdraw(@RequestHeader("X-User-Id") String userId, @RequestBody Map<String, BigDecimal> payload) {
        try {
            walletService.withdraw(userId, payload.get(AMOUNT_KEY));
            return ResponseEntity.ok("Withdrawal successful");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/me/history")
    public ResponseEntity<List<WalletTransaction>> getMyHistory(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(walletService.getHistory(userId));
    }

    @PostMapping("/{userId}/bid/hold")
    public ResponseEntity<String> holdForBid(@PathVariable String userId, @RequestBody Map<String, BigDecimal> payload) {
        try {
            walletService.holdForBid(userId, payload.get(AMOUNT_KEY));
            return ResponseEntity.ok("Funds held for bid");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{userId}/bid/release")
    public ResponseEntity<String> releaseFromBid(@PathVariable String userId, @RequestBody Map<String, BigDecimal> payload) {
        try {
            walletService.releaseFromBid(userId, payload.get(AMOUNT_KEY));
            return ResponseEntity.ok("Funds released");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{userId}/bid/pay")
    public ResponseEntity<String> payForWin(@PathVariable String userId, @RequestBody Map<String, BigDecimal> payload) {
        try {
            walletService.payFromHeld(userId, payload.get(AMOUNT_KEY));
            return ResponseEntity.ok("Payment successful from held funds");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}