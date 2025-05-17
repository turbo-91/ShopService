package org.example;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ShopService {
    private final OrderRepo orderRepo;
    private final ProductRepo productRepo;


    public List<Order> getOrdersByStatus(OrderStatus orderStatus) {
        return orderRepo.findByStatus(orderStatus);
    }

    public Order placeOrder(String id,
                            List<OrderItem> items,
                            OrderStatus orderStatus) {
        // validate each product exists
        items.forEach(item -> {
            // ensure item.product is present in the database
            String productId = item.getProduct().getId();
            productRepo.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));
        });

        // create and save a new Order document
        Order newOrder = new Order(
                id,
                items,
                orderStatus,
                Instant.now()
        );
        return orderRepo.save(newOrder);
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

        List<OrderItem> updatedItems = existing.getItems().stream()
                .map(item -> item.getProduct().getId().equals(productId)
                        ? item.withQuantity(newQuantity)
                        : item)
                .toList();

        boolean found = updatedItems.stream()
                .anyMatch(item -> item.getProduct().getId().equals(productId));

        if (!found) {
            throw new NoSuchElementException(
                    "Product not found in order: " + productId
            );
        }

        Order updatedOrder = existing.withItems(updatedItems);
        return orderRepo.save(updatedOrder);
    }
}
