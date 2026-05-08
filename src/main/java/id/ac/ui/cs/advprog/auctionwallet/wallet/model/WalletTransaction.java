package id.ac.ui.cs.advprog.auctionwallet.wallet.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallet_transactions")
@Getter
@NoArgsConstructor
@SuppressWarnings("PMD.ImmutableField")
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(precision = 19, scale = 2)
    private BigDecimal balanceBefore;

    @Column(precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(nullable = false)
    private ZonedDateTime createdAt;

    public WalletTransaction(String userId, TransactionType type, BigDecimal amount, 
                             BigDecimal balanceBefore, BigDecimal balanceAfter, String referenceId) {
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.referenceId = referenceId;
        this.createdAt = ZonedDateTime.now();
    }
}