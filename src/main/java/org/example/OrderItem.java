// src/main/java/org/example/OrderItem.java
package org.example;

import lombok.Value;
import lombok.With;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * An item inside an Order: holds the full Product and the ordered quantity.
 */
@Value
@With
public class OrderItem {
    @Field("product")
    Product product;

    @Field("quantity")
    int quantity;
}
