package org.example;

import lombok.Value;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Document(collection = "orders")
@Value
@With
public class Order {
    @Id
    String id;

    /**
     * Embedded list of items in this order.
     * (Assumes OrderItem has getters for product() and quantity().)
     */
    List<OrderItem> items;

    OrderStatus orderStatus;
    Instant orderTimestamp;

    /**
     * Calculate total price by summing product.price * quantity.
     */
    public BigDecimal totalPrice() {
        return items.stream()
                .map(item ->
                        item.product()
                                .price()
                                .multiply(BigDecimal.valueOf(item.quantity()))
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
