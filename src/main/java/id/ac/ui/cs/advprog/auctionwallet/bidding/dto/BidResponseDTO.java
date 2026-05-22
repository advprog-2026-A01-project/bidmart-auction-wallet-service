package id.ac.ui.cs.advprog.auctionwallet.bidding.dto;

import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.BidStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BidResponseDTO {

    private Long bidId;

    private Long auctionId;

    private Long userId;

    private BigDecimal bidAmount;

    private BidStatus status;
}