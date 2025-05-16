package org.example;

import java.math.BigDecimal;
import java.util.List;


public  record Order(String id, List<OrderItem> items, OrderStatus orderStatus
) {
    public BigDecimal totalPrice() {
        return items.stream()
                .map(item ->
                        item.product().price()
                                .multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
