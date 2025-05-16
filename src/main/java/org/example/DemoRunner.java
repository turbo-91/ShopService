package org.example;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DemoRunner implements CommandLineRunner {
    private final ProductRepo productRepo;
    private final ShopService shopService;

    @Override
    public void run(String... args) {
        // 1) Seed some products
        Product apple  = new Product("1", "Apple",  new BigDecimal("0.99"));
        Product banana = new Product("2", "Banana", new BigDecimal("0.59"));
        productRepo.save(apple);
        productRepo.save(banana);

        // 2) Build an order
        List<OrderItem> items = List.of(
                new OrderItem(apple,  3),  // 3 × €0.99 = €2.97
                new OrderItem(banana, 5)   // 5 × €0.59 = €2.95
        );

        // 3) Place order and print total
        Order order = shopService.placeOrder(
                "order1",
                items,
                OrderStatus.COMPLETED
        );
        System.out.println("Total price: €" + order.totalPrice());
    }
}
