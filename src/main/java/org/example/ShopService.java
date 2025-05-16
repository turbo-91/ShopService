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
        return orderRepo.findByOrderStatus(orderStatus);
    }

    public Order placeOrder(String id,
                            List<OrderItem> items,
                            OrderStatus orderStatus) {
        // validate each product exists
        items.forEach(item ->
                productRepo.findById(item.product().id())
                        .orElseThrow(() -> new ProductNotFoundException(item.product().id()))
        );

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

        var updatedItems = existing.items().stream()
                .map(item -> item.product().id().equals(productId)
                        ? new OrderItem(item.product(), newQuantity)
                        : item)
                .toList();

        if (updatedItems.stream().noneMatch(item -> item.product().id().equals(productId))) {
            throw new NoSuchElementException("Product not found in order: " + productId);
        }

        Order updated = existing.withItems(updatedItems);
        return orderRepo.save(updated);
    }
}
