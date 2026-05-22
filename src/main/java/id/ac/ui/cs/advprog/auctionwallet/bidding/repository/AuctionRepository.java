package id.ac.ui.cs.advprog.auctionwallet.bidding.repository;

import id.ac.ui.cs.advprog.auctionwallet.bidding.model.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

    @Query("select a.id from Auction a")
    List<Long> findAllIds();
}
