package org.example;

import lombok.With;
import java.math.BigDecimal;
import java.util.List;

@With
public record Order(
        String id,
        List<OrderItem> items,
        OrderStatus orderStatus
) {
    public BigDecimal totalPrice() {
        return items.stream()
                .map(item ->
                        item.product().price()
                                .multiply(BigDecimal.valueOf(item.quantity()))
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
