package org.example;

import java.math.BigDecimal;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // You can switch to new OrderListRepo() if preferred:
        OrderRepo orderRepo = new OrderMapRepo();
        ProductRepo productRepo = new ProductRepo();
        ShopService shopService = new ShopService(orderRepo, productRepo);

        // Example: add a product and place an order
        Product p1 = new Product("1", "Example Product", "cool", new BigDecimal("1.50"), 1);
        productRepo.addProduct(p1);

        Order order = shopService.placeOrder("order1", List.of(p1), new BigDecimal("1.50"));
        System.out.println("Placed order: " + order);
    }
}