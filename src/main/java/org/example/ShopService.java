package org.example;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ShopService {
    private final OrderRepo orderRepo;
    private final ProductRepo productRepo;

    public ShopService(OrderRepo orderRepo, ProductRepo productRepo) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
    }

    public List<Order> getOrdersByStatus(OrderStatus orderStatus) {
        return orderRepo.findByOrderStatus(orderStatus);
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
                Instant.now()
        );
        orderRepo.save(newOrder);
        return newOrder;
    }

    public Order updateOrderStatus(String orderId, OrderStatus newStatus) {
        Order existing = orderRepo.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));
        Order updated = existing.withOrderStatus(newStatus);
        return orderRepo.save(updated);
    }


    public Order updateOrderItemQuantity(String orderId,
                                         String productId,
                                         int newQuantity) {
        Order existing = orderRepo.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));

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
        return orderRepo.save(updated);
    }

}
