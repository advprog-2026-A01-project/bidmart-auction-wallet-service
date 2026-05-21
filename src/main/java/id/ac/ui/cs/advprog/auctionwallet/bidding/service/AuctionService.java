package id.ac.ui.cs.advprog.auctionwallet.bidding.service;

import id.ac.ui.cs.advprog.auctionwallet.bidding.dto.BidResponseDTO;
import id.ac.ui.cs.advprog.auctionwallet.bidding.exception.AuctionNotFoundException;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private static final long EXTENSION_MINUTES = 2;

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final WalletService walletService;
    private final AuctionValidationService validationService;
    private final BidRefundService bidRefundService;

    public List<Auction> getAllAuctions() {
        return auctionRepository.findAll();
    }

    public Auction getAuctionById(Long auctionId) {
        return auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionNotFoundException(auctionId));
    }

    @Transactional
    public BidResponseDTO placeBid(
            Long auctionId,
            Long userId,
            BigDecimal bidAmount
    ) {

        Auction auction = auctionRepository
                .findById(auctionId)
                .orElseThrow(() ->
                        new AuctionNotFoundException(auctionId)
                );

        validationService.validateAuction(auction);
        validationService.validateBidAmount(
                auction,
                bidAmount
        );

        String referenceId =
                generateReferenceId(auctionId);

        walletService.holdForBid(
                String.valueOf(userId),
                bidAmount,
                referenceId
        );

        bidRefundService.refundPreviousBidder(
                auction,
                referenceId
        );

        extendAuctionIfNeeded(auction);

        auction.updateHighestBid(
                userId,
                bidAmount
        );

        auctionRepository.save(auction);

        Bid bid = createBid(
                auctionId,
                userId,
                bidAmount
        );

        Bid savedBid = bidRepository.save(bid);

        return mapToResponse(savedBid);
    }

    private void extendAuctionIfNeeded(
            Auction auction
    ) {

        long minutesLeft =
                ChronoUnit.MINUTES.between(
                        LocalDateTime.now(),
                        auction.getEndTime()
                );

        if (minutesLeft < EXTENSION_MINUTES) {
            auction.extendAuction(
                    EXTENSION_MINUTES
            );
        }
    }

    private Bid createBid(
            Long auctionId,
            Long userId,
            BigDecimal bidAmount
    ) {

        Bid bid = new Bid();

        bid.setAuctionId(auctionId);
        bid.setUserId(userId);
        bid.setBidAmount(bidAmount);
        bid.setTimestamp(LocalDateTime.now());

        bid.markAsActive();

        return bid;
    }

    private String generateReferenceId(
            Long auctionId
    ) {
        return "BID-AUC-"
                + auctionId
                + "-"
                + System.currentTimeMillis();
    }

    private BidResponseDTO mapToResponse(
            Bid bid
    ) {

        return new BidResponseDTO(
                bid.getId(),
                bid.getAuctionId(),
                bid.getUserId(),
                bid.getBidAmount(),
                bid.getStatus()
        );
    }
}