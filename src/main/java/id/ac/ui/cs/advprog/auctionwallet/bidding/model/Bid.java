package id.ac.ui.cs.advprog.auctionwallet.bidding.model;

import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.BidStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "bids")
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long auctionId;

    private Long userId;

    private BigDecimal bidAmount;

    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private BidStatus status;

    // =========================
    // BUSINESS METHODS
    // =========================

    public void markAsActive() {
        this.status = BidStatus.ACTIVE;
    }

    public void markAsOutbid() {
        this.status = BidStatus.OUTBID;
    }

    public void markAsRefunded() {
        this.status = BidStatus.REFUNDED;
    }

    public void markAsWon() {
        this.status = BidStatus.WON;
    }

    public boolean isActive() {
        return this.status == BidStatus.ACTIVE;
    }

    public boolean isRefunded() {
        return this.status == BidStatus.REFUNDED;
    }
}