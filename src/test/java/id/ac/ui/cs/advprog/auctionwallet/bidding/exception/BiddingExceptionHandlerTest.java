package id.ac.ui.cs.advprog.auctionwallet.bidding.exception;

import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BiddingExceptionHandlerTest {

    private final BiddingExceptionHandler handler =
            new BiddingExceptionHandler();

    @Test
    void testHandleAuctionNotFoundStatus() {

        AuctionNotFoundException exception =
                new AuctionNotFoundException(1L);

        HttpStatusCode status =
                handler.handleAuctionNotFound(exception)
                        .getStatusCode();

        assertEquals(
                HttpStatus.NOT_FOUND,
                status,
                "Status should be NOT_FOUND"
        );
    }

    @Test
    void testHandleAuctionNotFoundMessage() {

        AuctionNotFoundException exception =
                new AuctionNotFoundException(1L);

        String message =
                handler.handleAuctionNotFound(exception)
                        .getBody();

        assertEquals(
                "Auction with id 1 not found",
                message,
                "Message should match"
        );
    }

    @Test
    void testHandleInvalidBidStatus() {

        InvalidBidException exception =
                new InvalidBidException(
                        "Invalid bid"
                );

        HttpStatusCode status =
                handler.handleInvalidBid(exception)
                        .getStatusCode();

        assertEquals(
                HttpStatus.BAD_REQUEST,
                status,
                "Status should be BAD_REQUEST"
        );
    }

    @Test
    void testHandleInvalidBidMessage() {

        InvalidBidException exception =
                new InvalidBidException(
                        "Invalid bid"
                );

        String message =
                handler.handleInvalidBid(exception)
                        .getBody();

        assertEquals(
                "Invalid bid",
                message,
                "Message should match"
        );
    }
}