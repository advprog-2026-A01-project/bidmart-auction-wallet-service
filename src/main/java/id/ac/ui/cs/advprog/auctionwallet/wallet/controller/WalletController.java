package id.ac.ui.cs.advprog.auctionwallet.wallet.controller;

import id.ac.ui.cs.advprog.auctionwallet.wallet.dto.AuctionWalletRequest;
import id.ac.ui.cs.advprog.auctionwallet.wallet.dto.TopUpRequest;
import id.ac.ui.cs.advprog.auctionwallet.wallet.dto.WithdrawRequest;
import id.ac.ui.cs.advprog.auctionwallet.wallet.model.Wallet;
import id.ac.ui.cs.advprog.auctionwallet.wallet.model.WalletTransaction;
import id.ac.ui.cs.advprog.auctionwallet.wallet.service.WalletService;
import jakarta.validation.Valid;
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

    private static final String HEADER_USER_ID = "X-User-Id";
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/me/info")
    public ResponseEntity<Map<String, BigDecimal>> getMyWalletInfo(@RequestHeader(HEADER_USER_ID) String userId) {
        Wallet wallet = walletService.getWallet(userId);
        return ResponseEntity.ok(Map.of(
                "availableBalance", wallet.getAvailableBalance(),
                "heldBalance", wallet.getHeldBalance()
        ));
    }

    @PostMapping("/me/topup")
    public ResponseEntity<String> myTopUp(@RequestHeader(HEADER_USER_ID) String userId, @Valid @RequestBody TopUpRequest payload) {
        walletService.topUp(userId, payload.getAmount());
        return ResponseEntity.ok("Top-up successful");
    }

    @PostMapping("/me/withdraw")
    public ResponseEntity<String> myWithdraw(@RequestHeader(HEADER_USER_ID) String userId, @Valid @RequestBody WithdrawRequest payload) {
        walletService.withdraw(userId, payload.getAmount());
        return ResponseEntity.ok("Withdrawal successful");
    }

    @GetMapping("/me/history")
    public ResponseEntity<List<WalletTransaction>> getMyHistory(@RequestHeader(HEADER_USER_ID) String userId) {
        return ResponseEntity.ok(walletService.getHistory(userId));
    }

    @PostMapping("/{userId}/bid/hold")
    public ResponseEntity<String> holdForBid(@PathVariable String userId, @Valid @RequestBody AuctionWalletRequest payload) {
        walletService.holdForBid(userId, payload.getAmount(), payload.getReferenceId());
        return ResponseEntity.ok("Funds held for bid");
    }

    @PostMapping("/{userId}/bid/release")
    public ResponseEntity<String> releaseFromBid(@PathVariable String userId, @Valid @RequestBody AuctionWalletRequest payload) {
        walletService.releaseFromBid(userId, payload.getAmount(), payload.getReferenceId());
        return ResponseEntity.ok("Funds released");
    }

    @PostMapping("/{userId}/bid/pay")
    public ResponseEntity<String> payForWin(@PathVariable String userId, @Valid @RequestBody AuctionWalletRequest payload) {
        walletService.payFromHeld(userId, payload.getAmount(), payload.getReferenceId());
        return ResponseEntity.ok("Payment successful from held funds");
    }
}