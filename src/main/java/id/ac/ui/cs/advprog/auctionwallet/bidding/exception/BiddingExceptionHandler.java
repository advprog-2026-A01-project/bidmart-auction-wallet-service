package id.ac.ui.cs.advprog.auctionwallet.bidding.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BiddingExceptionHandler {

    @ExceptionHandler(AuctionNotFoundException.class)
    public ResponseEntity<String> handleAuctionNotFound(
            AuctionNotFoundException e
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(InvalidBidException.class)
    public ResponseEntity<String> handleInvalidBid(
            InvalidBidException e
    ) {
        return ResponseEntity
                .badRequest()
                .body(e.getMessage());
    }
}