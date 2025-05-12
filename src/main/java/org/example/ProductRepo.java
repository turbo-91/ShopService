package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class ProductRepo {
    List<Product> products = new ArrayList<>();

    public List<Product> getProducts(){
        return products;
    }

    public Product getProduct(String id) {
        for (Product product : products) {
            if (Objects.equals(product.id(), id)) {
                return product;
            }
        }
        throw new NoSuchElementException("No product with id " + id);
    }

    public void addProduct(Product product){
        products.add(product);
    }
    public void removeProduct(Product product){
        products.remove(product);
    }
}
