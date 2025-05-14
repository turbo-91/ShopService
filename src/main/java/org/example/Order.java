package org.example;

import java.util.List;

public  record Order(String id,
                     List<OrderItem> items
) {
}
