package id.ac.ui.cs.advprog.auctionwallet.bidding.service;

import id.ac.ui.cs.advprog.auctionwallet.bidding.exception.InvalidBidException;
import id.ac.ui.cs.advprog.auctionwallet.bidding.model.Auction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class AuctionValidationService {

    public void validateAuction(Auction auction) {

        if (!auction.isBiddable()) {
            throw new InvalidBidException(
                    "Auction is no longer active"
            );
        }

        if (auction.isExpired(LocalDateTime.now())) {
            auction.close();

            throw new InvalidBidException(
                    "Auction has already closed"
            );
        }
    }

    public void validateBidAmount(
            Auction auction,
            BigDecimal bidAmount
    ) {
        BigDecimal minimumRequiredBid =
                auction.getMinimumRequiredBid();

        if (bidAmount.compareTo(minimumRequiredBid) < 0) {
            throw new InvalidBidException(
                    "Bid amount must be at least "
                            + minimumRequiredBid
            );
        }
    }
}