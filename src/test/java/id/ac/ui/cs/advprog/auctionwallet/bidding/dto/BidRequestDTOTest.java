package id.ac.ui.cs.advprog.auctionwallet.bidding.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BidRequestDTOTest {

    @Test
    void testGetterAndSetterUserId() {

        BidRequestDTO dto = new BidRequestDTO();

        dto.setUserId(1L);

        assertEquals(
                1L,
                dto.getUserId(),
                "User id should match"
        );
    }

    @Test
    void testGetterAndSetterAmount() {

        BidRequestDTO dto = new BidRequestDTO();

        dto.setAmount(BigDecimal.valueOf(100));

        assertEquals(
                BigDecimal.valueOf(100),
                dto.getAmount(),
                "Amount should match"
        );
    }
}