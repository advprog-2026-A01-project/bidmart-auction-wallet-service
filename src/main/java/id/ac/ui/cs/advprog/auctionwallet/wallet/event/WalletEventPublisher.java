package id.ac.ui.cs.advprog.auctionwallet.wallet.event;

import id.ac.ui.cs.advprog.auctionwallet.wallet.config.RabbitMQConfig;
import id.ac.ui.cs.advprog.auctionwallet.wallet.model.TransactionType;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class WalletEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public WalletEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishBalanceChangeEvent(String userId, TransactionType type, BigDecimal amount, BigDecimal newBalance) {
        Map<String, Object> event = new HashMap<>();
        event.put("userId", userId);
        event.put("transactionType", type.name());
        event.put("amount", amount);
        event.put("newBalance", newBalance);
        
        String routingKey = "wallet.balance.changed";
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, event);
    }
}
