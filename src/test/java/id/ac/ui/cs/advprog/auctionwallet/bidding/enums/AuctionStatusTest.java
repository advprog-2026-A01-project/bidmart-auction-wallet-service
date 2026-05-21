package id.ac.ui.cs.advprog.auctionwallet.bidding.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuctionStatusTest {

    @Test
    void testActiveIsBiddable() {

        assertEquals(
                true,
                AuctionStatus.ACTIVE.isBiddable(),
                "ACTIVE should be biddable"
        );
    }

    @Test
    void testExtendedIsBiddable() {

        assertEquals(
                true,
                AuctionStatus.EXTENDED.isBiddable(),
                "EXTENDED should be biddable"
        );
    }

    @Test
    void testDraftIsEditable() {

        assertEquals(
                true,
                AuctionStatus.DRAFT.isEditable(),
                "DRAFT should be editable"
        );
    }

    @Test
    void testWonIsTerminal() {

        assertEquals(
                true,
                AuctionStatus.WON.isTerminal(),
                "WON should be terminal"
        );
    }

    @Test
    void testClosedIsTerminal() {

        assertEquals(
                true,
                AuctionStatus.CLOSED.isTerminal(),
                "CLOSED should be terminal"
        );
    }
}