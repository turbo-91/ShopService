package org.example;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String productId) {
        super("Product with id '" + productId + "' not found.");
    }
}
