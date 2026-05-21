package id.ac.ui.cs.advprog.auctionwallet.bidding.service;

import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.AuctionStatus;
import id.ac.ui.cs.advprog.auctionwallet.bidding.exception.InvalidBidException;
import id.ac.ui.cs.advprog.auctionwallet.bidding.model.Auction;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuctionValidationServiceTest {

    private final AuctionValidationService validationService =
            new AuctionValidationService();

    @Test
    void testValidateAuctionThrowsExceptionWhenInactive() {

        Auction auction = new Auction();

        auction.setStatus(AuctionStatus.CLOSED);

        assertThrows(
                InvalidBidException.class,
                () -> validationService.validateAuction(auction),
                "Exception should be thrown when auction inactive"
        );
    }

    @Test
    void testValidateAuctionInactiveExceptionMessage() {

        Auction auction = new Auction();

        auction.setStatus(AuctionStatus.CLOSED);

        try {
            validationService.validateAuction(auction);
        } catch (InvalidBidException exception) {

            assertEquals(
                    "Auction is no longer active",
                    exception.getMessage(),
                    "Exception message should match"
            );
        }
    }

    @Test
    void testValidateAuctionThrowsExceptionWhenExpired() {

        Auction auction = new Auction();

        auction.setStatus(AuctionStatus.ACTIVE);

        auction.setEndTime(
                LocalDateTime.now().minusMinutes(1)
        );

        assertThrows(
                InvalidBidException.class,
                () -> validationService.validateAuction(auction),
                "Exception should be thrown when auction expired"
        );
    }

    @Test
    void testValidateAuctionExpiredExceptionMessage() {

        Auction auction = new Auction();

        auction.setStatus(AuctionStatus.ACTIVE);

        auction.setEndTime(
                LocalDateTime.now().minusMinutes(1)
        );

        try {
            validationService.validateAuction(auction);
        } catch (InvalidBidException exception) {

            assertEquals(
                    "Auction has already closed",
                    exception.getMessage(),
                    "Exception message should match"
            );
        }
    }

    @Test
    void testValidateAuctionSuccess() {

        Auction auction = new Auction();

        auction.setStatus(AuctionStatus.ACTIVE);

        auction.setEndTime(
                LocalDateTime.now().plusMinutes(10)
        );

        assertDoesNotThrow(
                () -> validationService.validateAuction(auction),
                "Validation should pass for active auction"
        );
    }

    @Test
    void testValidateBidAmountThrowsException() {

        Auction auction = new Auction();

        auction.setCurrentHighestBid(
                BigDecimal.valueOf(100)
        );

        auction.setMinimumIncrement(
                BigDecimal.valueOf(10)
        );

        assertThrows(
                InvalidBidException.class,
                () -> validationService.validateBidAmount(
                        auction,
                        BigDecimal.valueOf(105)
                ),
                "Exception should be thrown for invalid bid"
        );
    }

    @Test
    void testValidateBidAmountExceptionMessage() {

        Auction auction = new Auction();

        auction.setCurrentHighestBid(
                BigDecimal.valueOf(100)
        );

        auction.setMinimumIncrement(
                BigDecimal.valueOf(10)
        );

        try {
            validationService.validateBidAmount(
                    auction,
                    BigDecimal.valueOf(105)
            );
        } catch (InvalidBidException exception) {

            assertEquals(
                    "Bid amount must be at least 110",
                    exception.getMessage(),
                    "Exception message should match"
            );
        }
    }

    @Test
    void testValidateBidAmountSuccess() {

        Auction auction = new Auction();

        auction.setCurrentHighestBid(
                BigDecimal.valueOf(100)
        );

        auction.setMinimumIncrement(
                BigDecimal.valueOf(10)
        );

        assertDoesNotThrow(
                () -> validationService.validateBidAmount(
                        auction,
                        BigDecimal.valueOf(120)
                ),
                "Validation should pass for valid bid"
        );
    }
}