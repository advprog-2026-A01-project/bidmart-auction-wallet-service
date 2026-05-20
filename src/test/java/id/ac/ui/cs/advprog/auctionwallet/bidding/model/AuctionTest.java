package id.ac.ui.cs.advprog.auctionwallet.bidding.model;

import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.AuctionStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuctionTest {

    @Test
    void testIsBiddableReturnsTrueWhenActive() {

        Auction auction = new Auction();
        auction.setStatus(AuctionStatus.ACTIVE);

        assertTrue(auction.isBiddable());
    }

    @Test
    void testIsBiddableReturnsFalseWhenClosed() {

        Auction auction = new Auction();
        auction.setStatus(AuctionStatus.CLOSED);

        assertFalse(auction.isBiddable());
    }

    @Test
    void testIsExpiredReturnsTrue() {

        Auction auction = new Auction();

        auction.setEndTime(
                LocalDateTime.now().minusMinutes(1)
        );

        assertTrue(
                auction.isExpired(LocalDateTime.now())
        );
    }

    @Test
    void testGetMinimumRequiredBid() {

        Auction auction = new Auction();

        auction.setCurrentHighestBid(
                BigDecimal.valueOf(100)
        );

        auction.setMinimumIncrement(
                BigDecimal.valueOf(10)
        );

        assertEquals(
                BigDecimal.valueOf(110),
                auction.getMinimumRequiredBid()
        );
    }

    @Test
    void testUpdateHighestBid() {

        Auction auction = new Auction();

        auction.updateHighestBid(
                1L,
                BigDecimal.valueOf(500)
        );

        assertEquals(
                1L,
                auction.getCurrentHighestBidderId()
        );

        assertEquals(
                BigDecimal.valueOf(500),
                auction.getCurrentHighestBid()
        );
    }

    @Test
    void testCloseAuction() {

        Auction auction = new Auction();

        auction.close();

        assertEquals(
                AuctionStatus.CLOSED,
                auction.getStatus()
        );
    }
}