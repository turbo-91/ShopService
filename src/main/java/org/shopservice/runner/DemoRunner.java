package org.shopservice.runner;

import lombok.RequiredArgsConstructor;
import org.shopservice.model.*;
import org.shopservice.model.enums.OrderStatus;
import org.shopservice.repository.ProductRepo;
import org.shopservice.service.ShopService;
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
        productRepo.save(new Product(
                "1",
                "T-Shirt",
                "ACME Apparel",
                "100% cotton crew neck t-shirt",
                "Red",
                "M",
                new BigDecimal("19.99"),
                50
        ));
        productRepo.save(new Product(
                "2",
                "Slim Jeans",
                "DenimCo",
                "Blue slim-fit denim jeans",
                "Blue",
                "32",
                new BigDecimal("49.99"),
                30
        ));
        System.out.println();
        System.out.println("[SCENARIO A] Seeded products: T-Shirt and Slim Jeans");
        System.out.println("Stocks: T-Shirt=" + productRepo.findById("1").get().getStock()
                + ", Slim Jeans=" + productRepo.findById("2").get().getStock());
        System.out.println();

        // 2) Place a new order (PENDING)
        List<OrderItem> items1 = List.of(
                new OrderItem(productRepo.findById("1").orElseThrow(), 2)
        );
        Order order1 = shopService.placeOrder("order1", items1, OrderStatus.PROCESSING);
        System.out.println("[SCENARIO B] Placed order1 for 2×T-Shirt");
        System.out.println("Items: " + order1.getItems());
        System.out.println("Status: " + order1.getStatus());
        System.out.println("Timestamp: " + order1.getTimestamp());
        System.out.println("Remaining T-Shirt stock=" + productRepo.findById("1").get().getStock());
        System.out.println();

        // 3) Query orders by status PENDING
        List<Order> pendingOrders = shopService.getOrdersByStatus(OrderStatus.PROCESSING);
        System.out.println("[SCENARIO C] Pending orders count: " + pendingOrders.size());
        System.out.println();

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

        // 7) Update quantity in order2
        Order updatedOrder2 = shopService.updateOrderItemQuantity(order2.getId(), "2", 5);
        System.out.println("[SCENARIO G] Updated order2 Banana quantity: " +
                updatedOrder2.getItems().stream()
                        .filter(item -> item.getProduct().getId().equals("2"))
                        .findFirst().map(OrderItem::getQuantity).orElse(0));
        System.out.println("Items after update: " + order2.getItems());
        System.out.println(" ");

        // 8) Demonstrate goodsIn: restock apples
        shopService.goodsIn("1", 10);
        System.out.println("[SCENARIO H] Restocked T-Shirts by 10: stock="
                + productRepo.findById("1").get().getStock());
        System.out.println();

        // 9) Demonstrate goodsOut: ship bananas
        shopService.goodsOut("2", 5);
        System.out.println("[SCENARIO I] Shipped 5×Slim Jeans: stock="
                + productRepo.findById("2").get().getStock());
        System.out.println();

        // 10) Reserve stock in a cart
        List<CartItem> cartItems = List.of(
                new CartItem("1", 3),   // 3×T-Shirt
                new CartItem("2", 2)    // 2×Slim Jeans
        );
        Cart cart = shopService.reserveStockForCart("cart1", cartItems);
        System.out.println("[SCENARIO J] Reserved stock for cart1: " + cart.getItems());
        System.out.println("Stocks after reserve: T-Shirt="
                + productRepo.findById("1").get().getStock()
                + ", Slim Jeans=" + productRepo.findById("2").get().getStock());
        System.out.println();

        // 11) Calculate cart total without placing an order
        BigDecimal total = shopService.calculateCartTotal(cartItems);
        System.out.println("[SCENARIO K] Cart total: €" + total);
        System.out.println();

        System.out.println("[SCENARIO L] searchProducts(\"blue\"):");
        List<Product> blues = shopService.searchProducts("blue");
        System.out.println("Search results (" + blues.size() + "): ");
        blues.forEach(p -> System.out.println(" - " + p.getName() + ","
                + " color: [" + p.getColor() + "]"));
        System.out.println();

        // 12) Cancel the order and restock
        Order canceledOrder = shopService.cancelOrder(order1.getId());
        System.out.println("[SCENARIO M1] Canceled order1: status=" + canceledOrder.getStatus());
        System.out.println("T-Shirt stock after cancellation=" + productRepo.findById("1").get().getStock());

        // 13) Verify order is no longer in pending list
        List<Order> pendingAfterCancel = shopService.getOrdersByStatus(OrderStatus.PROCESSING);
        System.out.println("[SCENARIO M2] Pending orders after cancel: " + pendingAfterCancel.size());
        System.out.println();

        // 14) mark order completed
        Order completedOrder2 = shopService.updateOrderStatus(order2.getId(), OrderStatus.COMPLETED);
        System.out.println("[SCENARIO N] Completed order2: status=" + completedOrder2.getStatus());

        // 15) order 2 gets refunded
        Order refundedOrder2 = shopService.refundOrder(order2.getId());
        System.out.println("[SCENARIO O] Refunded order2: status=" + refundedOrder2.getStatus());
        System.out.println("Slim Jeans stock after refund: "
                + productRepo.findById("2").orElseThrow().getStock());
        System.out.println();

        // 16) Manual release of reserved stock ===
        System.out.println("=== SCENARIO P: Manual release of reserved stock ===");

// 1) Reserve 3 units of product "1"
        String demoCartId = "cart-manual-release";
        List<CartItem> demoItems = List.of(new CartItem("1", 3));
        shopService.reserveStockForCart(demoCartId, demoItems);

// 2) Check stock after reservation
        int afterReserve = productRepo.findById("1")
                .orElseThrow().getStock();
        System.out.println("Stock after reservation: " + afterReserve);

// 3) Manually release the reserved stock
        shopService.releaseReservedStock("1", 3);

// 4) Check stock after release
        int afterRelease = productRepo.findById("1")
                .orElseThrow().getStock();
        System.out.println("Stock after manual release: " + afterRelease);
        System.out.println();


        // Final state of orders by status
        System.out.println("[SCENARIO OMEGA-I] Final PENDING orders? " + shopService.getOrdersByStatus(OrderStatus.PROCESSING).size() + " orders found");
        System.out.println("Final PENDING orders: " + shopService.getOrdersByStatus(OrderStatus.PROCESSING));
        System.out.println();
        System.out.println("[SCENARIO OMEGA-II] Final COMPLETE orders? " + shopService.getOrdersByStatus(OrderStatus.COMPLETED).size() + " orders found");
        System.out.println("Final COMPLETED orders: " + shopService.getOrdersByStatus(OrderStatus.COMPLETED));
    }
}
