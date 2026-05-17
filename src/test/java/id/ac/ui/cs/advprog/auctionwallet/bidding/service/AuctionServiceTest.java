package id.ac.ui.cs.advprog.auctionwallet.bidding.service;

import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.AuctionStatus;
import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.BidStatus;
import id.ac.ui.cs.advprog.auctionwallet.bidding.model.Auction;
import id.ac.ui.cs.advprog.auctionwallet.bidding.model.Bid;
import id.ac.ui.cs.advprog.auctionwallet.bidding.repository.AuctionRepository;
import id.ac.ui.cs.advprog.auctionwallet.bidding.repository.BidRepository;
import id.ac.ui.cs.advprog.auctionwallet.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private AuctionService auctionService;

    private Auction auction;

    @BeforeEach
    void setUp() {
        auction = new Auction();
        auction.setId(1L);
        auction.setStatus(AuctionStatus.ACTIVE);
        auction.setEndTime(LocalDateTime.now().plusHours(1));
        auction.setCurrentHighestBid(new BigDecimal("10000"));
        auction.setMinimumIncrement(new BigDecimal("1000"));
        auction.setCurrentHighestBidderId(2L);
    }

    @Test
    void placeBid_Success_WithRefundPreviousBidder() {
        Long newUserId = 3L;
        BigDecimal newBidAmount = new BigDecimal("15000");

        Bid previousBid = new Bid();
        previousBid.setUserId(2L);
        previousBid.setBidAmount(new BigDecimal("10000"));
        previousBid.setStatus(BidStatus.ACTIVE);

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(bidRepository.findTopByAuctionIdAndStatusOrderByBidAmountDesc(1L, BidStatus.ACTIVE))
                .thenReturn(Optional.of(previousBid));

        when(bidRepository.save(any(Bid.class))).thenAnswer(i -> i.getArguments()[0]);

        Bid result = auctionService.placeBid(1L, newUserId, newBidAmount);

        assertNotNull(result);
        assertEquals(newUserId, result.getUserId());
        assertEquals(BidStatus.ACTIVE, result.getStatus());
        assertEquals(newBidAmount, auction.getCurrentHighestBid());

        verify(walletService, times(1)).holdForBid(eq(String.valueOf(newUserId)), eq(newBidAmount), anyString());

        verify(walletService, times(1)).releaseFromBid(eq("2"), eq(new BigDecimal("10000")), anyString());
    }

    @Test
    void placeBid_Fail_BidAmountTooLow() {
        Long newUserId = 3L;
        BigDecimal lowBidAmount = new BigDecimal("10500");

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            auctionService.placeBid(1L, newUserId, lowBidAmount);
        });

        assertTrue(exception.getMessage().contains("Bid amount must be at least"));
        verify(walletService, never()).holdForBid(anyString(), any(BigDecimal.class), anyString());
    }
}