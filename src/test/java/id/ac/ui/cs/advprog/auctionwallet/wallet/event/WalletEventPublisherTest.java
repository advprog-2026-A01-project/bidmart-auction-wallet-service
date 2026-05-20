package id.ac.ui.cs.advprog.auctionwallet.wallet.event;

import id.ac.ui.cs.advprog.auctionwallet.wallet.config.RabbitMQConfig;
import id.ac.ui.cs.advprog.auctionwallet.wallet.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({
    "PMD.UnitTestShouldIncludeAssert",
    "PMD.UnitTestContainsTooManyAsserts",
    "PMD.UnitTestAssertionsShouldIncludeMessage",
    "PMD.LawOfDemeter"
})
class WalletEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private WalletEventPublisher eventPublisher;

    @Test
    void testPublishBalanceChangeEventSendsToCorrectExchangeAndKey() {
        eventPublisher.publishBalanceChangeEvent(
                "user-123", TransactionType.TOP_UP,
                new BigDecimal("1000.00"), new BigDecimal("11000.00"));

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq("wallet.balance.changed"),
                org.mockito.ArgumentMatchers.any(Map.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testPublishBalanceChangeEventPayloadContainsCorrectFields() {
        ArgumentCaptor<Map<String, Object>> payloadCaptor =
                ArgumentCaptor.forClass(Map.class);

        eventPublisher.publishBalanceChangeEvent(
                "user-123", TransactionType.WITHDRAWAL,
                new BigDecimal("500.00"), new BigDecimal("9500.00"));

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq("wallet.balance.changed"),
                payloadCaptor.capture());

        Map<String, Object> payload = payloadCaptor.getValue();
        assertEquals("user-123", payload.get("userId"));
        assertEquals("WITHDRAWAL", payload.get("transactionType"));
        assertEquals(new BigDecimal("500.00"), payload.get("amount"));
        assertEquals(new BigDecimal("9500.00"), payload.get("newBalance"));
    }

    @Test
    void testPublishBalanceChangeEventUsesHoldTransactionType() {
        eventPublisher.publishBalanceChangeEvent(
                "user-456", TransactionType.HOLD,
                new BigDecimal("2000.00"), new BigDecimal("8000.00"));

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq("wallet.balance.changed"),
                org.mockito.ArgumentMatchers.any(Map.class));
    }
}
