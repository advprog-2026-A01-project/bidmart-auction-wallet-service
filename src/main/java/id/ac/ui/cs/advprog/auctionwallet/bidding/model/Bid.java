package id.ac.ui.cs.advprog.auctionwallet.bidding.model;

import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.BidStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bids", indexes = {
        @Index(name = "idx_bids_auction_id", columnList = "auction_id"),
        @Index(name = "idx_bids_bidder_id", columnList = "bidder_id"),
        @Index(name = "idx_bids_placed_at", columnList = "placed_at DESC")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bid {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @Column(name = "bidder_id", nullable = false)
    private UUID bidderId;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BidStatus status;

    @Column(name = "hold_id")
    private UUID holdId;

    @CreationTimestamp
    @Column(name = "placed_at", updatable = false)
    private LocalDateTime placedAt;

    public static Bid create(Auction auction, UUID bidderId, BigDecimal amount, UUID holdId) {
        Bid bid = new Bid();
        bid.id = UUID.randomUUID();
        bid.auction = auction;
        bid.bidderId = bidderId;
        bid.amount = amount;
        bid.status = BidStatus.ACTIVE;
        bid.holdId = holdId;
        return bid;
    }

    void markOutbid() {
        this.status = BidStatus.OUTBID;
    }

    void markWon() {
        this.status = BidStatus.WON;
    }

    void markRefunded() {
        this.status = BidStatus.REFUNDED;
    }
}