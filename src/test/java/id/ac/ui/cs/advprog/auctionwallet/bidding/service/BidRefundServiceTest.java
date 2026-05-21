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

    private static final String REFERENCE_ID =
            "REF-1";

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
    void testRefundPreviousBidderSavesBidTwice() {

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
                REFERENCE_ID
        );

        verify(bidRepository, times(2))
                .save(previousBid);
    }

    @Test
    void testRefundPreviousBidderReleasesWalletBalance() {

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
                REFERENCE_ID
        );

        verify(walletService)
                .releaseFromBid(
                        "2",
                        BigDecimal.valueOf(200),
                        REFERENCE_ID
                );
    }

    @Test
    void testRefundPreviousBidderDoesNothingWhenNoHighestBidder() {

        auction.setCurrentHighestBidderId(null);

        refundService.refundPreviousBidder(
                auction,
                REFERENCE_ID
        );

        verifyNoInteractions(walletService);
    }

    @Test
    void testRefundPreviousBidderDoesNotSaveWhenNoHighestBidder() {

        auction.setCurrentHighestBidderId(null);

        refundService.refundPreviousBidder(
                auction,
                REFERENCE_ID
        );

        verify(bidRepository, never())
                .save(any(Bid.class));
    }

    @Test
    void testRefundPreviousBidderDoesNotReleaseWalletWhenNoBid() {

        when(
                bidRepository
                        .findTopByAuctionIdAndStatusOrderByBidAmountDesc(
                                1L,
                                BidStatus.ACTIVE
                        )
        ).thenReturn(Optional.empty());

        refundService.refundPreviousBidder(
                auction,
                REFERENCE_ID
        );

        verify(walletService, never())
                .releaseFromBid(
                        anyString(),
                        any(),
                        anyString()
                );
    }
}