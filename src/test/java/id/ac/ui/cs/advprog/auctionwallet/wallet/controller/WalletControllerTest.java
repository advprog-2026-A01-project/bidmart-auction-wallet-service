package id.ac.ui.cs.advprog.auctionwallet.wallet.controller;

import id.ac.ui.cs.advprog.auctionwallet.wallet.model.Wallet;
import id.ac.ui.cs.advprog.auctionwallet.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletController.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = new Wallet("user-123");
        wallet.addBalance(new BigDecimal("100000.00"));
    }

    @Test
    void testGetWalletInfo() throws Exception {
        when(walletService.getWallet("user-123")).thenReturn(wallet);

        mockMvc.perform(get("/api/wallet/me/info")
                .header("X-User-Id", "user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableBalance").value(100000.00))
                .andExpect(jsonPath("$.heldBalance").value(0.00));
    }

    @Test
    void testGetWalletInfoMissingHeaderThrowsError() throws Exception {
        mockMvc.perform(get("/api/wallet/me/info"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testTopUp() throws Exception {
        mockMvc.perform(post("/api/wallet/me/topup")
                .header("X-User-Id", "user-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 50000.00}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Top-up successful"));

        verify(walletService).topUp("user-123", new BigDecimal("50000.00"));
    }

    @Test
    void testWithdrawInsufficientBalance() throws Exception {
        doThrow(new IllegalArgumentException("Insufficient balance"))
                .when(walletService).withdraw("user-123", new BigDecimal("500000.00"));

        mockMvc.perform(post("/api/wallet/me/withdraw")
                .header("X-User-Id", "user-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 500000.00}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Insufficient balance"));
    }
}