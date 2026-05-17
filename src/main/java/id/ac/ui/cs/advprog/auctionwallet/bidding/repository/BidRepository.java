package id.ac.ui.cs.advprog.auctionwallet.bidding.repository;

import id.ac.ui.cs.advprog.auctionwallet.bidding.model.Bid;
import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.BidStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    Optional<Bid> findTopByAuctionIdAndStatus(Long auctionId, BidStatus status);

    Optional<Bid> findTopByAuctionIdAndStatusOrderByBidAmountDesc(Long auctionId, BidStatus status);
}