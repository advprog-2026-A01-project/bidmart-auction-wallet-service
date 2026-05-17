package id.ac.ui.cs.advprog.auctionwallet.bidding.service;

import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.AuctionStatus;
import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.BidStatus;
import id.ac.ui.cs.advprog.auctionwallet.bidding.model.Auction;
import id.ac.ui.cs.advprog.auctionwallet.bidding.model.Bid;
import id.ac.ui.cs.advprog.auctionwallet.bidding.repository.AuctionRepository;
import id.ac.ui.cs.advprog.auctionwallet.bidding.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final WalletService walletService;

    @Transactional
    public Bid placeBid(Long auctionId, Long userId, BigDecimal bidAmount) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        LocalDateTime now = LocalDateTime.now();

        if (auction.getStatus() != AuctionStatus.ACTIVE || now.isAfter(auction.getEndTime())) {
            throw new RuntimeException("Auction is no longer active");
        }

        BigDecimal minimumRequiredBid = auction.getCurrentHighestBid().add(auction.getMinimumIncrement());
        if (bidAmount.compareTo(minimumRequiredBid) < 0) {
            throw new RuntimeException("Bid amount must be at least " + minimumRequiredBid);
        }

        boolean isFundHeld = walletService.holdFunds(userId, bidAmount);
        if (!isFundHeld) {
            throw new RuntimeException("Insufficient wallet balance");
        }

        if (auction.getCurrentHighestBidderId() != null) {
            walletService.refundFunds(auction.getCurrentHighestBidderId(), auction.getCurrentHighestBid());

            Bid previousTopBid = bidRepository.findTopByAuctionIdAndStatus(auctionId, BidStatus.ACTIVE).orElse(null);

            if (previousTopBid != null) {
                previousTopBid.setStatus(BidStatus.OUTBID);
                bidRepository.save(previousTopBid);

                boolean isRefunded = walletService.refundFunds(previousTopBid.getUserId(), previousTopBid.getBidAmount());

                if (isRefunded) {
                    previousTopBid.setStatus(BidStatus.REFUNDED);
                    bidRepository.save(previousTopBid);
                }
            }
        }

        long minutesLeft = ChronoUnit.MINUTES.between(now, auction.getEndTime());
        if (minutesLeft < 2) {
            auction.setEndTime(now.plusMinutes(2));
        }

        auction.setCurrentHighestBid(bidAmount);
        auction.setCurrentHighestBidderId(userId);
        auctionRepository.save(auction);

        Bid newBid = new Bid();
        newBid.setAuctionId(auctionId);
        newBid.setUserId(userId);
        newBid.setBidAmount(bidAmount);
        newBid.setTimestamp(now);
        newBid.setStatus(BidStatus.ACTIVE);

        return bidRepository.save(newBid);
    }
}