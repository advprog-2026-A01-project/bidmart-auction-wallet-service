package id.ac.ui.cs.advprog.auctionwallet.bidding.service;

import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.AuctionStatus;
import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.BidStatus;
import id.ac.ui.cs.advprog.auctionwallet.bidding.model.Auction;
import id.ac.ui.cs.advprog.auctionwallet.bidding.model.Bid;
import id.ac.ui.cs.advprog.auctionwallet.bidding.repository.AuctionRepository;
import id.ac.ui.cs.advprog.auctionwallet.bidding.repository.BidRepository;
import id.ac.ui.cs.advprog.auctionwallet.wallet.service.WalletService;
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

        if (auction.getStatus() != AuctionStatus.ACTIVE && auction.getStatus() != AuctionStatus.EXTENDED) {
            throw new RuntimeException("Auction is no longer active");
        }
        if (now.isAfter(auction.getEndTime())) {
            auction.setStatus(AuctionStatus.CLOSED);
            auctionRepository.save(auction);
            throw new RuntimeException("Auction has already closed");
        }

        BigDecimal minimumRequiredBid = auction.getCurrentHighestBid().add(auction.getMinimumIncrement());
        if (bidAmount.compareTo(minimumRequiredBid) < 0) {
            throw new RuntimeException("Bid amount must be at least " + minimumRequiredBid);
        }

        String referenceId = "BID-AUC-" + auctionId + "-" + System.currentTimeMillis();

        try {
            walletService.holdForBid(String.valueOf(userId), bidAmount, referenceId);
        } catch (Exception e) {
            throw new RuntimeException("Bidding failed: " + e.getMessage());
        }

        if (auction.getCurrentHighestBidderId() != null) {
            Long previousBidderId = auction.getCurrentHighestBidderId();
            BigDecimal previousBidAmount = auction.getCurrentHighestBid();

            Bid previousTopBid = bidRepository.findTopByAuctionIdAndStatusOrderByBidAmountDesc(auctionId, BidStatus.ACTIVE)
                    .orElse(null);

            if (previousTopBid != null) {
                previousTopBid.setStatus(BidStatus.OUTBID);
                bidRepository.save(previousTopBid);

                try {
                    walletService.releaseFromBid(String.valueOf(previousBidderId), previousBidAmount, referenceId);

                    previousTopBid.setStatus(BidStatus.REFUNDED);
                    bidRepository.save(previousTopBid);
                } catch (Exception e) {
                    System.err.println("Failed to automatic refund for user " + previousBidderId + ": " + e.getMessage());
                }
            }
        }

        long minutesLeft = ChronoUnit.MINUTES.between(now, auction.getEndTime());
        if (minutesLeft < 2) {
            auction.setEndTime(now.plusMinutes(2));
            auction.setStatus(AuctionStatus.EXTENDED);
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