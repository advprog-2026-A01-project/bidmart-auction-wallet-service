package id.ac.ui.cs.advprog.auctionwallet.bidding.model;

import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.AuctionStatus;
import lombok.Data;
import jakarta.persistence.*;
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
}