package id.ac.ui.cs.advprog.auctionwallet.bidding.controller;

import id.ac.ui.cs.advprog.auctionwallet.bidding.dto.BidRequestDTO;
import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.BidStatus;
import id.ac.ui.cs.advprog.auctionwallet.bidding.model.Bid;
import id.ac.ui.cs.advprog.auctionwallet.bidding.service.AuctionService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;

import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.context.TestPropertySource;

import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuctionController.class)
@TestPropertySource(properties = {
        "gateway.secret=local-dev-gateway-secret"
})
@SuppressWarnings("deprecation")
class AuctionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuctionService auctionService;

    @Test
    @WithMockUser
    void testPlaceBid_Endpoint_Success() throws Exception {

        BidRequestDTO requestDTO = new BidRequestDTO();
        requestDTO.setUserId(3L);
        requestDTO.setAmount(new BigDecimal("15000"));

        Bid responseBid = new Bid();
        responseBid.setId(10L);
        responseBid.setAuctionId(1L);
        responseBid.setUserId(3L);
        responseBid.setBidAmount(new BigDecimal("15000"));
        responseBid.setStatus(BidStatus.ACTIVE);

        when(auctionService.placeBid(
                eq(1L),
                eq(3L),
                any(BigDecimal.class)
        )).thenReturn(responseBid);

        mockMvc.perform(post("/api/auctions/1/bids")
                        .with(csrf())
                        .header("X-Gateway-Secret", "local-dev-gateway-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.bidAmount").value(15000));
    }

    @Test
    @WithMockUser
    void testPlaceBid_Endpoint_Fail_BadRequest() throws Exception {

        BidRequestDTO requestDTO = new BidRequestDTO();
        requestDTO.setUserId(3L);
        requestDTO.setAmount(new BigDecimal("100"));

        when(auctionService.placeBid(
                eq(1L),
                eq(3L),
                any(BigDecimal.class)
        )).thenThrow(
                new RuntimeException("Bid amount must be at least 11000")
        );

        mockMvc.perform(post("/api/auctions/1/bids")
                        .with(csrf())
                        .header("X-Gateway-Secret", "local-dev-gateway-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Bid amount must be at least 11000"));
    }
}