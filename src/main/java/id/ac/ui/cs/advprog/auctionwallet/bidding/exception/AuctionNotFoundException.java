package id.ac.ui.cs.advprog.auctionwallet.bidding.exception;

public class AuctionNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AuctionNotFoundException(Long auctionId) {
        super("Auction with id " + auctionId + " not found");
    }
}