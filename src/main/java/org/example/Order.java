package org.example;

import lombok.Value;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Document(collection = "orders")
@Value
@With
public class Order {
    @Id
    String id;

    @Field("items")
    @With
    List<OrderItem> items;

    @Field("status")
    OrderStatus status;

    @Field("timestamp")
    Instant timestamp;

    public BigDecimal totalPrice() {
        return items.stream()
                .map(item ->
                        item.getProduct().getPrice()
                                .multiply(BigDecimal.valueOf(item.getQuantity()))
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Order withOrderStatus(OrderStatus newStatus) {
        return new Order(
                this.id,
                this.items,
                newStatus,
                this.timestamp
        );
    }


}
