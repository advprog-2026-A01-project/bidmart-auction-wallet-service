package id.ac.ui.cs.advprog.auctionwallet.bidding.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.auctionwallet.bidding.dto.BidRequestDTO;
import id.ac.ui.cs.advprog.auctionwallet.bidding.dto.BidResponseDTO;
import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.BidStatus;
import id.ac.ui.cs.advprog.auctionwallet.bidding.service.AuctionService;
import id.ac.ui.cs.advprog.auctionwallet.bidding.model.Auction;
import java.util.List;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;

import org.springframework.test.context.TestPropertySource;

import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuctionController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "gateway.secret=local-dev-gateway-secret"
})
@SuppressWarnings({
        "deprecation",
        "PMD.UnitTestShouldIncludeAssert"
})
class AuctionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuctionService auctionService;

    @Test
    void testPlaceBidEndpoint() throws Exception {

        BidRequestDTO request =
                new BidRequestDTO();

        request.setUserId(1L);
        request.setAmount(
                BigDecimal.valueOf(200)
        );

        BidResponseDTO response =
                new BidResponseDTO(
                        1L,
                        1L,
                        1L,
                        BigDecimal.valueOf(200),
                        BidStatus.ACTIVE
                );

        when(auctionService.placeBid(
                eq(1L),
                eq(1L),
                any(BigDecimal.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/auctions/1/bids")
                                .header(
                                        "X-Gateway-Secret",
                                        "local-dev-gateway-secret"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bidAmount")
                        .value(200))
                .andExpect(jsonPath("$.status")
                        .value("ACTIVE"));
    }

    @Test
    void testGetAllAuctionsEndpoint() throws Exception {
        Auction auction = new Auction();
        auction.setId(1L);
        auction.setCurrentHighestBid(BigDecimal.valueOf(100));
        auction.setMinimumIncrement(BigDecimal.valueOf(10));
        auction.setStatus(BidStatus.ACTIVE == null ? null : id.ac.ui.cs.advprog.auctionwallet.bidding.enums.AuctionStatus.ACTIVE);
        when(auctionService.getAllAuctions()).thenReturn(Collections.singletonList(auction));

        mockMvc.perform(
                        get("/api/auctions")
                                .header("X-Gateway-Secret", "local-dev-gateway-secret")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void testGetAuctionByIdEndpoint() throws Exception {
        Auction auction = new Auction();
        auction.setId(1L);
        auction.setCurrentHighestBid(BigDecimal.valueOf(100));
        auction.setMinimumIncrement(BigDecimal.valueOf(10));
        auction.setStatus(id.ac.ui.cs.advprog.auctionwallet.bidding.enums.AuctionStatus.ACTIVE);
        when(auctionService.getAuctionById(1L)).thenReturn(auction);

        mockMvc.perform(
                        get("/api/auctions/1")
                                .header("X-Gateway-Secret", "local-dev-gateway-secret")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}