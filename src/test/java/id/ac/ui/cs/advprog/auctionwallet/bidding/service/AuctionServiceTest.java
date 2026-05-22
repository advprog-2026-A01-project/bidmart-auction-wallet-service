package id.ac.ui.cs.advprog.auctionwallet.bidding.service;

import id.ac.ui.cs.advprog.auctionwallet.bidding.dto.BidResponseDTO;
import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.AuctionStatus;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({
        "PMD.TooManyMethods",
        "PMD.AvoidDuplicateLiterals",
        "PMD.UnitTestContainsTooManyAsserts",
        "PMD.UnitTestAssertionsShouldIncludeMessage",
        "PMD.UnitTestShouldIncludeAssert"
})
class AuctionServiceTest {

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private WalletService walletService;

    @Mock
    private AuctionValidationService validationService;

    @Mock
    private BidRefundService refundService;

    @InjectMocks
    private AuctionService auctionService;

    private Auction auction;

    @BeforeEach
    void setUp() {

        auction = new Auction();

        auction.setId(1L);

        auction.setStatus(
                AuctionStatus.ACTIVE
        );

        auction.setCurrentHighestBid(
                BigDecimal.valueOf(100)
        );

        auction.setMinimumIncrement(
                BigDecimal.valueOf(10)
        );

        auction.setEndTime(
                LocalDateTime.now().plusMinutes(10)
        );
    }

    @Test
    @SuppressWarnings("PMD.UnitTestContainsTooManyAsserts")
    void testPlaceBidSuccess() {

        when(auctionRepository.findById(1L))
                .thenReturn(Optional.of(auction));

        when(bidRepository.save(any(Bid.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        BidResponseDTO response =
                auctionService.placeBid(
                        1L,
                        2L,
                        BigDecimal.valueOf(150)
                );

        assertEquals(
                BigDecimal.valueOf(150),
                response.getBidAmount(),
                "Bid amount should be 150"
        );

        verify(walletService)
                .holdForBid(
                        anyString(),
                        any(),
                        anyString()
                );

        verify(auctionRepository)
                .save(any(Auction.class));

        verify(bidRepository)
                .save(any(Bid.class));
    }

    @Test
    void testPlaceBidAuctionNotFound() {

        when(auctionRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> auctionService.placeBid(
                        99L,
                        1L,
                        BigDecimal.valueOf(100)
                ),
                "Should throw exception"
        );
    }

    @Test
    void testPlaceBidExtendsAuction() {

        auction.setEndTime(
                LocalDateTime.now().plusSeconds(30)
        );

        when(auctionRepository.findById(1L))
                .thenReturn(Optional.of(auction));

        when(bidRepository.save(any(Bid.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        auctionService.placeBid(
                1L,
                2L,
                BigDecimal.valueOf(200)
        );

        assertEquals(
                AuctionStatus.EXTENDED,
                auction.getStatus(),
                "Auction should be extended"
        );
    }

    @Test
    @SuppressWarnings("PMD.UnitTestContainsTooManyAsserts")
    void testGetAllAuctions() {
        when(auctionRepository.findAll()).thenReturn(java.util.List.of(auction));
        java.util.List<Auction> result = auctionService.getAllAuctions();
        assertEquals(1, result.size(), "Result size should be 1");
        assertEquals(auction, result.get(0), "Result should contain the mocked auction");
    }

    @Test
    void testGetAuctionByIdSuccess() {
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        Auction result = auctionService.getAuctionById(1L);
        assertEquals(auction, result, "Result should be the mocked auction");
    }

    @Test
    void testGetAuctionByIdNotFound() {
        when(auctionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(
                id.ac.ui.cs.advprog.auctionwallet.bidding.exception.AuctionNotFoundException.class,
                () -> auctionService.getAuctionById(99L),
                "Should throw AuctionNotFoundException"
        );
    }

    @Test
    @SuppressWarnings("PMD.UnitTestContainsTooManyAsserts")
    void testCreateAuctionSetsFieldsAndReturns() {
        when(auctionRepository.save(any(Auction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        java.time.LocalDateTime start = java.time.LocalDateTime.now().plusDays(1);
        java.time.LocalDateTime end   = java.time.LocalDateTime.now().plusDays(2);

        Auction result = auctionService.createAuction(
                10L,
                BigDecimal.valueOf(500),
                BigDecimal.valueOf(50),
                start,
                end
        );

        assertEquals(10L, result.getItemId(), "Item ID should match");
        assertEquals(BigDecimal.valueOf(500), result.getStartingPrice(), "Starting price should match");
        assertEquals(id.ac.ui.cs.advprog.auctionwallet.bidding.enums.AuctionStatus.ACTIVE, result.getStatus(), "Status should be ACTIVE");
        verify(auctionRepository).save(any(Auction.class));
    }

    @Test
    void testCloseAuctionWithWinnerSettlesPayment() {
        auction.setCurrentHighestBidderId(2L);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(auctionRepository.save(any(Auction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Auction result = auctionService.closeAuction(1L);

        assertEquals(id.ac.ui.cs.advprog.auctionwallet.bidding.enums.AuctionStatus.WON, result.getStatus(), "Status should be WON");
        verify(walletService).payFromHeld(any(), any(), any());
    }

    @Test
    void testCloseAuctionWithoutBidderSetsUnsold() {
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(auctionRepository.save(any(Auction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Auction result = auctionService.closeAuction(1L);

        assertEquals(id.ac.ui.cs.advprog.auctionwallet.bidding.enums.AuctionStatus.UNSOLD, result.getStatus(), "Status should be UNSOLD");
    }

    @Test
    void testCloseAuctionAlreadyTerminalReturnsImmediately() {
        auction.setStatus(id.ac.ui.cs.advprog.auctionwallet.bidding.enums.AuctionStatus.WON);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));

        Auction result = auctionService.closeAuction(1L);

        assertEquals(id.ac.ui.cs.advprog.auctionwallet.bidding.enums.AuctionStatus.WON, result.getStatus(), "Should return without further state change");
    }

    @Test
    void testCloseAuctionNotFoundThrows() {
        when(auctionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(
                id.ac.ui.cs.advprog.auctionwallet.bidding.exception.AuctionNotFoundException.class,
                () -> auctionService.closeAuction(99L)
        );
    }

    @Test
    @SuppressWarnings("PMD.UnitTestContainsTooManyAsserts")
    void testCancelUserBidsCancelsActiveBidsAndReleasesWallet() {
        id.ac.ui.cs.advprog.auctionwallet.bidding.model.Bid bid = new id.ac.ui.cs.advprog.auctionwallet.bidding.model.Bid();
        bid.setAuctionId(1L);
        bid.setUserId(2L);
        bid.setBidAmount(BigDecimal.valueOf(200));
        bid.markAsActive();

        when(bidRepository.findByUserIdAndStatus(2L, id.ac.ui.cs.advprog.auctionwallet.bidding.enums.BidStatus.ACTIVE))
                .thenReturn(java.util.List.of(bid));
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(bidRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        int count = auctionService.cancelUserBids(2L);

        assertEquals(1, count, "Should cancel 1 bid");
        verify(walletService).releaseFromBid(any(), any(), any());
    }

    @Test
    void testCancelUserBidsSkipsTerminalAuctions() {
        auction.setStatus(id.ac.ui.cs.advprog.auctionwallet.bidding.enums.AuctionStatus.WON);

        id.ac.ui.cs.advprog.auctionwallet.bidding.model.Bid bid = new id.ac.ui.cs.advprog.auctionwallet.bidding.model.Bid();
        bid.setAuctionId(1L);
        bid.setUserId(2L);
        bid.setBidAmount(BigDecimal.valueOf(200));
        bid.markAsActive();

        when(bidRepository.findByUserIdAndStatus(2L, id.ac.ui.cs.advprog.auctionwallet.bidding.enums.BidStatus.ACTIVE))
                .thenReturn(java.util.List.of(bid));
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));

        int count = auctionService.cancelUserBids(2L);

        assertEquals(0, count, "Terminal auction bids should be skipped");
    }
}