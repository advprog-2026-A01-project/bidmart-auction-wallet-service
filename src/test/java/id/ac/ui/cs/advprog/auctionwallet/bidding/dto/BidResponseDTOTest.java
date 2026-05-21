package id.ac.ui.cs.advprog.auctionwallet.bidding.dto;

import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.BidStatus;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BidResponseDTOTest {

    @Test
    void testConstructorBidId() {

        BidResponseDTO dto =
                new BidResponseDTO(
                        1L,
                        2L,
                        3L,
                        BigDecimal.valueOf(100),
                        BidStatus.ACTIVE
                );

        assertEquals(
                1L,
                dto.getBidId(),
                "Bid id should match"
        );
    }

    @Test
    void testConstructorAuctionId() {

        BidResponseDTO dto =
                new BidResponseDTO(
                        1L,
                        2L,
                        3L,
                        BigDecimal.valueOf(100),
                        BidStatus.ACTIVE
                );

        assertEquals(
                2L,
                dto.getAuctionId(),
                "Auction id should match"
        );
    }

    @Test
    void testConstructorUserId() {

        BidResponseDTO dto =
                new BidResponseDTO(
                        1L,
                        2L,
                        3L,
                        BigDecimal.valueOf(100),
                        BidStatus.ACTIVE
                );

        assertEquals(
                3L,
                dto.getUserId(),
                "User id should match"
        );
    }

    @Test
    void testConstructorBidAmount() {

        BidResponseDTO dto =
                new BidResponseDTO(
                        1L,
                        2L,
                        3L,
                        BigDecimal.valueOf(100),
                        BidStatus.ACTIVE
                );

        assertEquals(
                BigDecimal.valueOf(100),
                dto.getBidAmount(),
                "Bid amount should match"
        );
    }

    @Test
    void testConstructorStatus() {

        BidResponseDTO dto =
                new BidResponseDTO(
                        1L,
                        2L,
                        3L,
                        BigDecimal.valueOf(100),
                        BidStatus.ACTIVE
                );

        assertEquals(
                BidStatus.ACTIVE,
                dto.getStatus(),
                "Status should match"
        );
    }
}