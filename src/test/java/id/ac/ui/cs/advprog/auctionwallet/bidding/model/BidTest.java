package id.ac.ui.cs.advprog.auctionwallet.bidding.model;

import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.BidStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BidTest {

    @Test
    void testMarkAsActive() {

        Bid bid = new Bid();

        bid.markAsActive();

        assertEquals(
                BidStatus.ACTIVE,
                bid.getStatus(),
                "Bid status should be ACTIVE"
        );
    }

    @Test
    void testMarkAsOutbid() {

        Bid bid = new Bid();

        bid.markAsOutbid();

        assertEquals(
                BidStatus.OUTBID,
                bid.getStatus(),
                "Bid status should be OUTBID"
        );
    }

    @Test
    void testMarkAsRefunded() {

        Bid bid = new Bid();

        bid.markAsRefunded();

        assertEquals(
                BidStatus.REFUNDED,
                bid.getStatus(),
                "Bid status should be REFUNDED"
        );
    }

    @Test
    void testIsActiveReturnsTrue() {

        Bid bid = new Bid();

        bid.markAsActive();

        assertTrue(
                bid.isActive(),
                "Bid should be active"
        );
    }
}