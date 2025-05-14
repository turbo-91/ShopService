package org.example;

import java.math.BigDecimal;
import java.util.List;

public  record Order(String id,
                     List<OrderItem> items,
                     BigDecimal totalPrice) {
}
