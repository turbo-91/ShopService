package org.shopservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;

@Document(collection = "products")
@Data
@AllArgsConstructor
public class Product {
    @Id
    private String id; // Now mutable
    private String name;
    private String brand;
    private String description;
    private String color;
    private String size;
    private BigDecimal price;
    private int stock;
}
