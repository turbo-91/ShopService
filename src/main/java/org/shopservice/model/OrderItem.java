package org.shopservice.model;

import lombok.Value;
import lombok.With;
import org.springframework.data.mongodb.core.mapping.Field;

@Value
@With
public class OrderItem {
    @Field("product")
    Product product;

    @Field("quantity")
    int quantity;
}