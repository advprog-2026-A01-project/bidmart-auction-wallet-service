package id.ac.ui.cs.advprog.auctionwallet.wallet.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "wallets")
@Getter
@NoArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal heldBalance = BigDecimal.ZERO;

    public Wallet(String userId) {
        this.userId = userId;
    }

    public void addBalance(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.availableBalance = this.availableBalance.add(amount);
    }

    public void withdrawBalance(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (this.availableBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        this.availableBalance = this.availableBalance.subtract(amount);
    }

    public void holdBalance(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (this.availableBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        this.availableBalance = this.availableBalance.subtract(amount);
        this.heldBalance = this.heldBalance.add(amount);
    }

    public void releaseBalance(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (this.heldBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient held balance");
        }
        this.heldBalance = this.heldBalance.subtract(amount);
        this.availableBalance = this.availableBalance.add(amount);
    }

    public void deductHeldBalance(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (this.heldBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient held balance");
        }
        this.heldBalance = this.heldBalance.subtract(amount);
    }
}