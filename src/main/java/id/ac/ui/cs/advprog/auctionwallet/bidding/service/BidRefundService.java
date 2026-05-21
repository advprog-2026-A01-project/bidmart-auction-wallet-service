package id.ac.ui.cs.advprog.auctionwallet.bidding.service;

import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.BidStatus;
import id.ac.ui.cs.advprog.auctionwallet.bidding.model.Auction;
import id.ac.ui.cs.advprog.auctionwallet.bidding.model.Bid;
import id.ac.ui.cs.advprog.auctionwallet.bidding.repository.BidRepository;
import id.ac.ui.cs.advprog.auctionwallet.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BidRefundService {

    private final BidRepository bidRepository;
    private final WalletService walletService;

    public void refundPreviousBidder(
            Auction auction,
            String referenceId
    ) {

        if (auction.getCurrentHighestBidderId() == null) {
            return;
        }

        Bid previousTopBid =
                bidRepository
                        .findTopByAuctionIdAndStatusOrderByBidAmountDesc(
                                auction.getId(),
                                BidStatus.ACTIVE
                        )
                        .orElse(null);

        if (previousTopBid == null) {
            return;
        }

        previousTopBid.markAsOutbid();
        bidRepository.save(previousTopBid);

        walletService.releaseFromBid(
                String.valueOf(
                        auction.getCurrentHighestBidderId()
                ),
                auction.getCurrentHighestBid(),
                referenceId
        );

        previousTopBid.markAsRefunded();
        bidRepository.save(previousTopBid);
    }
}