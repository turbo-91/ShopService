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
    public void run(String... args) throws Exception {
        // Step 1: Seed products (Product.java)
        System.out.println("[DemoRunner.java] Step 1: Seeding products (see Product.java)");
        Product apple = new Product("1", "Apple", new BigDecimal("0.99"), 100);
        Product banana = new Product("2", "Banana", new BigDecimal("0.59"), 200);
        productRepo.save(apple);
        productRepo.save(banana);

        // Step 2: Build order items (OrderItem.java)
        System.out.println("[DemoRunner.java] Step 2: Building OrderItem list (see OrderItem.java)");
        List<OrderItem> items = List.of(
                new OrderItem(apple,  3),
                new OrderItem(banana, 5)
        );

        // Step 3: Place order via service (ShopService.java)
        System.out.println("[DemoRunner.java] Step 3: Placing order via ShopService (see ShopService.java)");
        Order order = shopService.placeOrder(
                "order1",
                items,
                OrderStatus.COMPLETED
        );

        // Step 4: Compute total price (Order.java)
        System.out.println("[DemoRunner.java] Step 4: Calculating total price (see Order.java)");
        BigDecimal total = order.totalPrice();
        System.out.println("Total price: €" + total);
    }
}


