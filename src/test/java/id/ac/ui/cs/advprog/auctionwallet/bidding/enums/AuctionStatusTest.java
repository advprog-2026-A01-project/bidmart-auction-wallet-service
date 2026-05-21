package id.ac.ui.cs.advprog.auctionwallet.bidding.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class AuctionStatusTest {

    @ParameterizedTest
    @EnumSource(value = AuctionStatus.class, names = {"ACTIVE", "EXTENDED"})
    void testIsBiddableTrue(AuctionStatus status) {
        assertTrue(status.isBiddable(), "Status should be biddable");
    }

    @ParameterizedTest
    @EnumSource(value = AuctionStatus.class, names = {"DRAFT", "CLOSED", "WON", "UNSOLD"})
    void testIsBiddableFalse(AuctionStatus status) {
        assertFalse(status.isBiddable(), "Status should not be biddable");
    }

    @ParameterizedTest
    @EnumSource(value = AuctionStatus.class, names = {"DRAFT"})
    void testIsEditableTrue(AuctionStatus status) {
        assertTrue(status.isEditable(), "Status should be editable");
    }

    @ParameterizedTest
    @EnumSource(value = AuctionStatus.class, names = {"ACTIVE", "EXTENDED", "CLOSED", "WON", "UNSOLD"})
    void testIsEditableFalse(AuctionStatus status) {
        assertFalse(status.isEditable(), "Status should not be editable");
    }

    @ParameterizedTest
    @EnumSource(value = AuctionStatus.class, names = {"DRAFT", "ACTIVE"})
    void testIsCancellableTrue(AuctionStatus status) {
        assertTrue(status.isCancellable(), "Status should be cancellable");
    }

    @ParameterizedTest
    @EnumSource(value = AuctionStatus.class, names = {"EXTENDED", "CLOSED", "WON", "UNSOLD"})
    void testIsCancellableFalse(AuctionStatus status) {
        assertFalse(status.isCancellable(), "Status should not be cancellable");
    }

    @ParameterizedTest
    @EnumSource(value = AuctionStatus.class, names = {"WON", "UNSOLD", "CLOSED"})
    void testIsTerminalTrue(AuctionStatus status) {
        assertTrue(status.isTerminal(), "Status should be terminal");
    }

    @ParameterizedTest
    @EnumSource(value = AuctionStatus.class, names = {"DRAFT", "ACTIVE", "EXTENDED"})
    void testIsTerminalFalse(AuctionStatus status) {
        assertFalse(status.isTerminal(), "Status should not be terminal");
    }

    @Test
    void testEnumValuesLength() {
        assertEquals(6, AuctionStatus.values().length, "There should be 6 enum values");
    }

    @Test
    void testEnumValueOfDraft() {
        assertNotNull(AuctionStatus.valueOf("DRAFT"), "DRAFT status should exist");
    }
}