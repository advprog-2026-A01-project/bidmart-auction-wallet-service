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
    void testFindTopBid() {

        Bid bid1 = new Bid();
        bid1.setAuctionId(1L);
        bid1.setUserId(1L);
        bid1.setBidAmount(BigDecimal.valueOf(100));
        bid1.setTimestamp(LocalDateTime.now());
        bid1.setStatus(BidStatus.ACTIVE);

        Bid bid2 = new Bid();
        bid2.setAuctionId(1L);
        bid2.setUserId(2L);
        bid2.setBidAmount(BigDecimal.valueOf(200));
        bid2.setTimestamp(LocalDateTime.now());
        bid2.setStatus(BidStatus.ACTIVE);

        bidRepository.save(bid1);
        bidRepository.save(bid2);

        Optional<Bid> result =
                bidRepository
                        .findTopByAuctionIdAndStatusOrderByBidAmountDesc(
                                1L,
                                BidStatus.ACTIVE
                        );

        assertTrue(result.isPresent());

        assertEquals(
                BigDecimal.valueOf(200),
                result.get().getBidAmount()
        );
    }
}