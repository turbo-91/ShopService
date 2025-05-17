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
        // 1) Seed product catalog
        productRepo.save(new Product("1", "Apple",  new BigDecimal("0.99"), 100));
        productRepo.save(new Product("2", "Banana", new BigDecimal("0.59"), 200));
        System.out.println(" ");
        System.out.println("[SCENARIO A] Seeded products: Apple and Banana");
        System.out.println(" ");

        // 2) Place a new order (PENDING)
        List<OrderItem> items1 = List.of(
                new OrderItem(productRepo.findById("1").orElseThrow(), 3)
        );
        Order order1 = shopService.placeOrder("order1", items1, OrderStatus.PROCESSING);
        System.out.println("[SCENARIO B] Placed order1 (PENDING):");
        System.out.println("Items: " + order1.getItems());
        System.out.println("Status: " + order1.getStatus());
        System.out.println("Timestamp: " + order1.getTimestamp());
        System.out.println(" ");

        // 3) Query orders by status PENDING
        List<Order> pendingOrders = shopService.getOrdersByStatus(OrderStatus.PROCESSING);
        System.out.println("[SCENARIO C] Pending orders count: " + pendingOrders.size());

        // 4) Update order1 status to COMPLETED
        Order completedOrder = shopService.updateOrderStatus(order1.getId(), OrderStatus.COMPLETED);
        System.out.println("[SCENARIO D] Updated order1 to COMPLETED: ");
        System.out.println("order 1: " + completedOrder);
        System.out.println(" ");

        // 5) Query orders by status COMPLETED
        List<Order> completedOrders = shopService.getOrdersByStatus(OrderStatus.COMPLETED);
        System.out.println("[SCENARIO E] Completed orders count: " + completedOrders.size());
        System.out.println(" ");

        // 6) Place a second order (PENDING)
        List<OrderItem> items2 = List.of(
                new OrderItem(productRepo.findById("1").orElseThrow(), 1),
                new OrderItem(productRepo.findById("2").orElseThrow(), 2)
        );
        Order order2 = shopService.placeOrder("order2", items2, OrderStatus.PROCESSING);
        System.out.println("[SCENARIO F] Placed order2 (PENDING):");
        System.out.println("Items: " + order2.getItems());
        System.out.println("Status: " + order2.getStatus());
        System.out.println("Timestamp: " + order2.getTimestamp());
        System.out.println(" ");

        // 7) Update quantity of Banana in order2
        Order updatedOrder2 = shopService.updateOrderItemQuantity(order2.getId(), "2", 5);
        System.out.println("[SCENARIO G] Updated order2 Banana quantity: " +
                updatedOrder2.getItems().stream()
                        .filter(item -> item.getProduct().getId().equals("2"))
                        .findFirst().map(OrderItem::getQuantity).orElse(0));
        System.out.println("Items after update: " + order2.getItems());
        System.out.println(" ");

        // 8) Final state of orders by status
        System.out.println("[SCENARIO H] Final PENDING orders? " + shopService.getOrdersByStatus(OrderStatus.PROCESSING).size() + " orders found");
        System.out.println("Final PENDING orders: " + shopService.getOrdersByStatus(OrderStatus.PROCESSING));
        System.out.println(" ");
        System.out.println("[SCENARIO I] Final COMPLETE orders? " + shopService.getOrdersByStatus(OrderStatus.COMPLETED).size() + " orders found");
        System.out.println("Final COMPLETED orders: " + shopService.getOrdersByStatus(OrderStatus.COMPLETED));
    }
}
