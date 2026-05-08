package id.ac.ui.cs.advprog.auctionwallet.bidding.enums;

public enum AuctionStatus {
    DRAFT,
    ACTIVE,
    EXTENDED,
    CLOSED,
    WON,
    UNSOLD;

    public boolean isBiddable() {
        return this == ACTIVE || this == EXTENDED;
    }

    public boolean isEditable() {
        return this == DRAFT;
    }

    public boolean isCancellable() {
        return this == DRAFT || this == ACTIVE;
    }

    public boolean isTerminal() {
        return this == WON || this == UNSOLD || this == CLOSED;
    }
}