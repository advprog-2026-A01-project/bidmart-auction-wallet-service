package id.ac.ui.cs.advprog.auctionwallet.wallet.controller;

import id.ac.ui.cs.advprog.auctionwallet.wallet.config.GatewaySecretInterceptor;
import id.ac.ui.cs.advprog.auctionwallet.wallet.exception.GlobalExceptionHandler;
import id.ac.ui.cs.advprog.auctionwallet.wallet.exception.InsufficientBalanceException;
import id.ac.ui.cs.advprog.auctionwallet.wallet.model.TransactionType;
import id.ac.ui.cs.advprog.auctionwallet.wallet.model.Wallet;
import id.ac.ui.cs.advprog.auctionwallet.wallet.model.WalletTransaction;
import id.ac.ui.cs.advprog.auctionwallet.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WalletController.class)
@Import({GatewaySecretInterceptor.class, GlobalExceptionHandler.class, id.ac.ui.cs.advprog.auctionwallet.security.SecurityConfig.class})
@TestPropertySource(properties = {"gateway.secret=local-dev-gateway-secret"})
@SuppressWarnings({
    "PMD.UnitTestShouldIncludeAssert",
    "PMD.AvoidDuplicateLiterals",
    "PMD.UnitTestContainsTooManyAsserts",
    "PMD.UnitTestAssertionsShouldIncludeMessage",
    "PMD.TooManyMethods"
})
class WalletControllerTest {

    private static final String GATEWAY_HEADER  = "X-Gateway-Secret";
    private static final String GATEWAY_SECRET  = "local-dev-gateway-secret";
    private static final String USER_HEADER     = "X-User-Id";
    private static final String USER_ID         = "user-123";
    private static final String AUCTION_PAYLOAD = "{\"amount\": 30000.00, \"referenceId\": \"auc-001\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = new Wallet(USER_ID);
        wallet.addBalance(new BigDecimal("100000.00"));
    }

    @Test
    void testRequestWithoutGatewaySecretIsRejected() throws Exception {
        mockMvc.perform(get("/api/wallet/me/info")
                .header(USER_HEADER, USER_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRequestWithWrongGatewaySecretIsRejected() throws Exception {
        mockMvc.perform(get("/api/wallet/me/info")
                .header(GATEWAY_HEADER, "wrong-secret")
                .header(USER_HEADER, USER_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetWalletInfo() throws Exception {
        when(walletService.getWallet(USER_ID)).thenReturn(wallet);

        mockMvc.perform(get("/api/wallet/me/info")
                .header(GATEWAY_HEADER, GATEWAY_SECRET)
                .header(USER_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableBalance").value(100000.00))
                .andExpect(jsonPath("$.heldBalance").value(0.00));
    }

    @Test
    void testGetWalletInfoMissingUserHeaderReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/wallet/me/info")
                .header(GATEWAY_HEADER, GATEWAY_SECRET))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testTopUp() throws Exception {
        mockMvc.perform(post("/api/wallet/me/topup")
                .header(GATEWAY_HEADER, GATEWAY_SECRET)
                .header(USER_HEADER, USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 50000.00}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Top-up successful"));

        verify(walletService).topUp(USER_ID, new BigDecimal("50000.00"));
    }

    @Test
    void testTopUpWithNegativeAmountReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/wallet/me/topup")
                .header(GATEWAY_HEADER, GATEWAY_SECRET)
                .header(USER_HEADER, USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": -100.00}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testWithdrawSuccess() throws Exception {
        doNothing().when(walletService).withdraw(USER_ID, new BigDecimal("20000.00"));

        mockMvc.perform(post("/api/wallet/me/withdraw")
                .header(GATEWAY_HEADER, GATEWAY_SECRET)
                .header(USER_HEADER, USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 20000.00}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Withdrawal successful"));
    }

    @Test
    void testWithdrawInsufficientBalanceReturnsBadRequest() throws Exception {
        doThrow(new InsufficientBalanceException("Insufficient balance"))
                .when(walletService).withdraw(USER_ID, new BigDecimal("500000.00"));

        mockMvc.perform(post("/api/wallet/me/withdraw")
                .header(GATEWAY_HEADER, GATEWAY_SECRET)
                .header(USER_HEADER, USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 500000.00}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Insufficient balance"));
    }

    @Test
    void testGetHistory() throws Exception {
        WalletTransaction tx = new WalletTransaction(USER_ID, TransactionType.TOP_UP,
                new BigDecimal("50000.00"), BigDecimal.ZERO, new BigDecimal("50000.00"), null);
        when(walletService.getHistory(USER_ID)).thenReturn(List.of(tx));

        mockMvc.perform(get("/api/wallet/me/history")
                .header(GATEWAY_HEADER, GATEWAY_SECRET)
                .header(USER_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("TOP_UP"));
    }

    @Test
    void testHoldForBid() throws Exception {
        doNothing().when(walletService).holdForBid(USER_ID, new BigDecimal("30000.00"), "auc-001");

        mockMvc.perform(post("/api/wallet/{userId}/bid/hold", USER_ID)
                .header(GATEWAY_HEADER, GATEWAY_SECRET)
                .contentType(MediaType.APPLICATION_JSON)
                .content(AUCTION_PAYLOAD))
                .andExpect(status().isOk())
                .andExpect(content().string("Funds held for bid"));

        verify(walletService).holdForBid(USER_ID, new BigDecimal("30000.00"), "auc-001");
    }

    @Test
    void testHoldForBidInsufficientBalanceReturnsBadRequest() throws Exception {
        doThrow(new InsufficientBalanceException("Insufficient balance"))
                .when(walletService).holdForBid(USER_ID, new BigDecimal("30000.00"), "auc-001");

        mockMvc.perform(post("/api/wallet/{userId}/bid/hold", USER_ID)
                .header(GATEWAY_HEADER, GATEWAY_SECRET)
                .contentType(MediaType.APPLICATION_JSON)
                .content(AUCTION_PAYLOAD))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Insufficient balance"));
    }

    @Test
    void testReleaseFromBid() throws Exception {
        doNothing().when(walletService).releaseFromBid(USER_ID, new BigDecimal("30000.00"), "auc-001");

        mockMvc.perform(post("/api/wallet/{userId}/bid/release", USER_ID)
                .header(GATEWAY_HEADER, GATEWAY_SECRET)
                .contentType(MediaType.APPLICATION_JSON)
                .content(AUCTION_PAYLOAD))
                .andExpect(status().isOk())
                .andExpect(content().string("Funds released"));
    }

    @Test
    void testPayForWin() throws Exception {
        doNothing().when(walletService).payFromHeld(USER_ID, new BigDecimal("30000.00"), "auc-001");

        mockMvc.perform(post("/api/wallet/{userId}/bid/pay", USER_ID)
                .header(GATEWAY_HEADER, GATEWAY_SECRET)
                .contentType(MediaType.APPLICATION_JSON)
                .content(AUCTION_PAYLOAD))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment successful from held funds"));
    }
}
