package org.example;

import java.util.*;

public class ProductRepo {
    List<Product> products = new ArrayList<>();

    public List<Product> getProducts(){
        return products;
    }

    public Optional<Product> getProductById(String id) {
        return products.stream()
                .filter(product -> Objects.equals(product.id(), id))
                .findFirst();
    }

    public void addProduct(Product product){
        products.add(product);
    }
    public void removeProduct(Product product){
        products.remove(product);
    }
}
