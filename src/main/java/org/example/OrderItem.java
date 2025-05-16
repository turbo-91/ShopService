package org.example;

import lombok.Value;
import lombok.With;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;

@Value
@With
public class OrderItem {
    @Field("product")
    Product product;

    @Field("quantity")
    int quantity;
}