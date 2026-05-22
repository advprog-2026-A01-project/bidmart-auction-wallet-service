package id.ac.ui.cs.advprog.auctionwallet.bidding.dto;

import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.BidStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BidResponseDTOTest {

    @Test
    void testBidResponseDTOBidId() {

        BidResponseDTO dto =
                new BidResponseDTO(
                        1L,
                        2L,
                        3L,
                        BigDecimal.valueOf(500),
                        BidStatus.ACTIVE
                );

        assertEquals(
                1L,
                dto.getBidId(),
                "Bid id should match"
        );
    }

    @Test
    void testBidResponseDTOAuctionId() {

        BidResponseDTO dto =
                new BidResponseDTO(
                        1L,
                        2L,
                        3L,
                        BigDecimal.valueOf(500),
                        BidStatus.ACTIVE
                );

        assertEquals(
                2L,
                dto.getAuctionId(),
                "Auction id should match"
        );
    }

    @Test
    void testBidResponseDTOStatus() {

        BidResponseDTO dto =
                new BidResponseDTO(
                        1L,
                        2L,
                        3L,
                        BigDecimal.valueOf(500),
                        BidStatus.ACTIVE
                );

        assertEquals(
                BidStatus.ACTIVE,
                dto.getStatus(),
                "Status should match"
        );
    }
}