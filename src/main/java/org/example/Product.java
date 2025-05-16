package org.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;

@Document(collection = "products")
@Data@AllArgsConstructor
public class Product {
    @Id
    private final String id;
    private String name;
    private BigDecimal price;
    private int stock;
}
