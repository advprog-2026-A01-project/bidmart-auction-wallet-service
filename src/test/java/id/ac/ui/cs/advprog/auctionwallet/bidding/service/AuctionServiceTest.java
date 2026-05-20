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
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
        auction.setStatus(AuctionStatus.ACTIVE);
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
                response.getBidAmount()
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
}