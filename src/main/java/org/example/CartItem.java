package org.example;

import lombok.Value;
import lombok.With;
import org.springframework.data.mongodb.core.mapping.Field;

@Value
@With
public class CartItem {
    @Field("productId")
    String productId;
    @Field("quantity")
    int quantity;
}