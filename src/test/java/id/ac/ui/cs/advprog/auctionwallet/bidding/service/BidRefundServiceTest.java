package id.ac.ui.cs.advprog.auctionwallet.bidding.service;

import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.BidStatus;
import id.ac.ui.cs.advprog.auctionwallet.bidding.model.Auction;
import id.ac.ui.cs.advprog.auctionwallet.bidding.model.Bid;
import id.ac.ui.cs.advprog.auctionwallet.bidding.repository.BidRepository;
import id.ac.ui.cs.advprog.auctionwallet.wallet.service.WalletService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidRefundServiceTest {

    @Mock
    private BidRepository bidRepository;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private BidRefundService refundService;

    private Auction auction;

    @BeforeEach
    void setUp() {

        auction = new Auction();

        auction.setId(1L);

        auction.setCurrentHighestBidderId(2L);

        auction.setCurrentHighestBid(
                BigDecimal.valueOf(200)
        );
    }

    @Test
    void testRefundPreviousBidderSuccess() {

        Bid previousBid = new Bid();

        previousBid.setStatus(BidStatus.ACTIVE);

        when(
                bidRepository
                        .findTopByAuctionIdAndStatusOrderByBidAmountDesc(
                                1L,
                                BidStatus.ACTIVE
                        )
        ).thenReturn(Optional.of(previousBid));

        refundService.refundPreviousBidder(
                auction,
                "REF-1"
        );

        verify(bidRepository, times(2))
                .save(previousBid);

        verify(walletService)
                .releaseFromBid(
                        "2",
                        BigDecimal.valueOf(200),
                        "REF-1"
                );
    }

    @Test
    void testRefundPreviousBidderReturnsWhenNoHighestBidder() {

        auction.setCurrentHighestBidderId(null);

        refundService.refundPreviousBidder(
                auction,
                "REF-1"
        );

        verifyNoInteractions(walletService);

        verify(bidRepository, never())
                .save(any(Bid.class));
    }

    @Test
    void testRefundPreviousBidderReturnsWhenNoPreviousBid() {

        when(
                bidRepository
                        .findTopByAuctionIdAndStatusOrderByBidAmountDesc(
                                1L,
                                BidStatus.ACTIVE
                        )
        ).thenReturn(Optional.empty());

        refundService.refundPreviousBidder(
                auction,
                "REF-1"
        );

        verify(walletService, never())
                .releaseFromBid(
                        anyString(),
                        any(),
                        anyString()
                );
    }
}