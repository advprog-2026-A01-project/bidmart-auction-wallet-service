package id.ac.ui.cs.advprog.auctionwallet.bidding.exception;

public class AuctionNotFoundException extends RuntimeException {

    public AuctionNotFoundException(Long auctionId) {
        super("Auction with id " + auctionId + " not found");
    }
}