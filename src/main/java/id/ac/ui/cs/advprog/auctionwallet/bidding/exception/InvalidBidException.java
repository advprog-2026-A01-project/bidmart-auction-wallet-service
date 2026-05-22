package id.ac.ui.cs.advprog.auctionwallet.bidding.exception;

public class InvalidBidException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidBidException(String message) {
        super(message);
    }
}