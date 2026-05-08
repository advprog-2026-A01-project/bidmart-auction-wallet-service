package id.ac.ui.cs.advprog.auctionwallet.bidding.model;

import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.AuctionStatus;
import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.AuctionType;

import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.BidStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "auctions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "bids")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Auction {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "listing_id", nullable = false)
    private UUID listingId;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AuctionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "auction_type", nullable = false, length = 20)
    private AuctionType auctionType;

    @Column(name = "starting_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal startingPrice;

    @Column(name = "reserve_price", precision = 15, scale = 2)
    private BigDecimal reservePrice;

    @Column(name = "current_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "minimum_increment", nullable = false, precision = 15, scale = 2)
    private BigDecimal minimumIncrement;

    @Column(name = "available_slots")
    private Integer availableSlots;

    @Column(name = "winner_id")
    private UUID winnerId;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private LocalDateTime endsAt;

    @Column(name = "region_code", length = 50)
    private String regionCode;

    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("placedAt DESC")
    private List<Bid> bids = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    public static Auction create(UUID listingId, UUID sellerId, AuctionType type,
                                 BigDecimal startingPrice, BigDecimal reservePrice,
                                 BigDecimal minimumIncrement, LocalDateTime startsAt,
                                 LocalDateTime endsAt) {
        Auction auction = new Auction();
        auction.id = UUID.randomUUID();
        auction.listingId = listingId;
        auction.sellerId = sellerId;
        auction.auctionType = type;
        auction.startingPrice = startingPrice;
        auction.reservePrice = reservePrice;
        auction.currentPrice = startingPrice;
        auction.minimumIncrement = minimumIncrement;
        auction.startsAt = startsAt;
        auction.endsAt = endsAt;
        auction.status = AuctionStatus.DRAFT;
        return auction;
    }

    void activate() {
        this.status = AuctionStatus.ACTIVE;
    }

    void extend(LocalDateTime newEndsAt) {
        this.endsAt = newEndsAt;
        this.status = AuctionStatus.EXTENDED;
    }

    void close() {
        this.status = AuctionStatus.CLOSED;
    }

    void markWon(UUID winnerId) {
        this.status = AuctionStatus.WON;
        this.winnerId = winnerId;
    }

    void markUnsold() {
        this.status = AuctionStatus.UNSOLD;
    }

    void updateCurrentPrice(BigDecimal newPrice) {
        this.currentPrice = newPrice;
    }

    void addBid(Bid bid) {
        this.bids.add(bid);
    }

    public boolean isAntiSnipingTriggered(LocalDateTime bidTime, int windowMinutes) {
        return bidTime.isAfter(endsAt.minusMinutes(windowMinutes));
    }

    public boolean isReserveMet() {
        if (reservePrice == null) return true;
        return currentPrice.compareTo(reservePrice) >= 0;
    }

    public Bid getHighestBid() {
        return bids.stream()
                .filter(b -> b.getStatus() == BidStatus.ACTIVE)
                .findFirst()
                .orElse(null);
    }
}