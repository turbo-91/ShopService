package org.example;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class ShopService {
    private final OrderRepo orderRepo;
    private final ProductRepo productRepo;

    public ShopService(OrderRepo orderRepo, ProductRepo productRepo) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
    }

    public List<Order> getOrdersByStatus(OrderStatus orderStatus) {
        return orderRepo.getOrders().stream()
                .filter(order -> order.orderStatus() == orderStatus)
                .collect(Collectors.toList());
    }

    public Order placeOrder(String id,
                            List<OrderItem> items,
                            OrderStatus orderStatus) {
        // validate each product exists
        items.forEach(item ->
                productRepo.getProductById(item.product().id())
                        .orElseThrow(() -> new ProductNotFoundException(item.product().id()))
        );

        Order newOrder = new Order(
                id,
                items,
                orderStatus,
                Instant.now()           // stamp with now
        );
        orderRepo.addOrder(newOrder);
        return newOrder;
    }

    public Order updateOrderStatus(String orderId, OrderStatus newStatus) {
        Order existing = orderRepo.getOrder(orderId);
        Order updated = existing.withOrderStatus(newStatus);

        orderRepo.removeOrder(existing);
        orderRepo.addOrder(updated);

        return updated;
    }

    public Order updateOrderItemQuantity(String orderId,
                                         String productId,
                                         int newQuantity) {
        Order existing = orderRepo.getOrder(orderId);  // throws if missing

        List<OrderItem> updatedItems = existing.items().stream()
                .map(item ->
                        item.product().id().equals(productId)
                                ? new OrderItem(item.product(), newQuantity)
                                : item
                )
                .collect(Collectors.toList());

        boolean found = updatedItems.stream()
                .anyMatch(item -> item.product().id().equals(productId));

        if (!found) {
            throw new NoSuchElementException(
                    "Product not found in order: " + productId
            );
        }

        Order updated = existing.withItems(updatedItems);
        orderRepo.removeOrder(existing);
        orderRepo.addOrder(updated);
        return updated;
    }

}
