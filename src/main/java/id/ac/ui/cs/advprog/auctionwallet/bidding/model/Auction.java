package id.ac.ui.cs.advprog.auctionwallet.bidding.model;

import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.AuctionStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "auctions")
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long itemId;

    private BigDecimal startingPrice;
    private BigDecimal currentHighestBid;
    private BigDecimal minimumIncrement;

    private Long currentHighestBidderId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private AuctionStatus status;

    public boolean isBiddable() {
        return status.isBiddable();
    }

    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(endTime);
    }

    public BigDecimal getMinimumRequiredBid() {
        return currentHighestBid.add(minimumIncrement);
    }

    public void updateHighestBid(Long bidderId, BigDecimal bidAmount) {
        this.currentHighestBid = bidAmount;
        this.currentHighestBidderId = bidderId;
    }

    public void extendAuction(long minutes) {
        this.endTime = LocalDateTime.now().plusMinutes(minutes);
        this.status = AuctionStatus.EXTENDED;
    }

    public void close() {
        this.status = AuctionStatus.CLOSED;
    }
}