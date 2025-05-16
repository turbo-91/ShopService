package org.example;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // You can switch to new OrderListRepo() if preferred:
        OrderRepo orderRepo = new OrderMapRepo();
        ProductRepo productRepo = new ProductRepo();
        ShopService shopService = new ShopService(orderRepo, productRepo);

        Product apple = new Product("1", "Apple", new BigDecimal("0.99"));
        Product banana = new Product("2", "Banana", new BigDecimal("0.59"));

        productRepo.addProduct(apple);
        productRepo.addProduct(banana);

        List<OrderItem> items = List.of(
                new OrderItem(apple, 3),    // 3 * 0.99 = 2.97
                new OrderItem(banana, 5)    // 5 * 0.59 = 2.95
        );

        Order order = shopService.placeOrder("order1", items, OrderStatus.COMPLETED);
        System.out.println("Total price: €" + order.totalPrice());

    }
}