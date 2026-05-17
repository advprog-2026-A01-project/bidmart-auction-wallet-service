package id.ac.ui.cs.advprog.auctionwallet.bidding.model;

import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.AuctionStatus;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class AuctionTest {

    @Test
    void testCreateAuction() {
        Auction auction = new Auction();
        auction.setId(1L);
        auction.setItemId(100L);
        auction.setStartingPrice(new BigDecimal("10000"));
        auction.setMinimumIncrement(new BigDecimal("1000"));
        auction.setStatus(AuctionStatus.ACTIVE);
        auction.setEndTime(LocalDateTime.now().plusHours(1));

        assertEquals(1L, auction.getId());
        assertEquals(AuctionStatus.ACTIVE, auction.getStatus());
        assertEquals(new BigDecimal("10000"), auction.getStartingPrice());
    }
}