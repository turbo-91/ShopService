package org.shopservice.model;

import lombok.Value;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.List;

@Document(collection = "carts")
@Value
@With
public class Cart {
    @Id
    String id;
    List<CartItem> items;
    Instant createdAt;
}
