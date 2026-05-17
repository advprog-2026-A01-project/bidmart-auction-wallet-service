package id.ac.ui.cs.advprog.auctionwallet.bidding.repository;

import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.BidStatus;
import id.ac.ui.cs.advprog.auctionwallet.bidding.model.Bid;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BidRepositoryTest {

    @Autowired
    private BidRepository bidRepository;

    @Test
    void testFindTopByAuctionIdAndStatusOrderByBidAmountDesc() {
        Bid bid1 = new Bid();
        bid1.setAuctionId(1L);
        bid1.setBidAmount(new BigDecimal("50000"));
        bid1.setStatus(BidStatus.OUTBID);
        bid1.setTimestamp(LocalDateTime.now().minusMinutes(5));
        bidRepository.save(bid1);

        Bid bid2 = new Bid();
        bid2.setAuctionId(1L);
        bid2.setBidAmount(new BigDecimal("60000"));
        bid2.setStatus(BidStatus.ACTIVE);
        bid2.setTimestamp(LocalDateTime.now());
        bidRepository.save(bid2);

        Optional<Bid> topBid = bidRepository.findTopByAuctionIdAndStatusOrderByBidAmountDesc(1L, BidStatus.ACTIVE);

        assertTrue(topBid.isPresent());
        assertEquals(new BigDecimal("60000"), topBid.get().getBidAmount());
        assertEquals(BidStatus.ACTIVE, topBid.get().getStatus());
    }
}