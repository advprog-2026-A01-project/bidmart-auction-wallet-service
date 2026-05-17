package id.ac.ui.cs.advprog.auctionwallet.bidding.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BidRequestDTO {
    private Long userId;
    private BigDecimal amount;
}