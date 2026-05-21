package id.ac.ui.cs.advprog.auctionwallet.bidding.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuctionNotFoundExceptionTest {

    @Test
    void testExceptionMessage() {

        AuctionNotFoundException exception =
                new AuctionNotFoundException(1L);

        assertEquals(
                "Auction with id 1 not found",
                exception.getMessage(),
                "Exception message should match"
        );
    }
}