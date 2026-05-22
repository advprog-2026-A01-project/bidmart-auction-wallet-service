package id.ac.ui.cs.advprog.auctionwallet.bidding.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BidRequestDTOTest {

    @Test
    void testBidRequestDTOSetAndGetUserId() {

        BidRequestDTO dto =
                new BidRequestDTO();

        dto.setUserId(1L);

        assertEquals(
                1L,
                dto.getUserId(),
                "User id should match"
        );
    }

    @Test
    void testBidRequestDTOSetAndGetAmount() {

        BidRequestDTO dto =
                new BidRequestDTO();

        dto.setAmount(
                BigDecimal.valueOf(200)
        );

        assertEquals(
                BigDecimal.valueOf(200),
                dto.getAmount(),
                "Amount should match"
        );
    }
}