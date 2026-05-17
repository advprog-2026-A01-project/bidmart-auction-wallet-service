package id.ac.ui.cs.advprog.auctionwallet.bidding.model;

import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.BidStatus;
import lombok.Data;
import jakarta.persistence.*;
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
}