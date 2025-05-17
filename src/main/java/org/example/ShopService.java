package org.example;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ShopService {
    private final OrderRepo orderRepo;
    private final ProductRepo productRepo;
    private final CartRepo cartRepo;


    public List<Order> getOrdersByStatus(OrderStatus orderStatus) {
        return orderRepo.findByStatus(orderStatus);
    }

    public Order placeOrder(String id,
                            List<OrderItem> items,
                            OrderStatus orderStatus) {
        // Validate each product exists and decrement its stock
        for (OrderItem item : items) {
            String pid = item.getProduct().getId();
            Product product = productRepo.findById(pid)
                    .orElseThrow(() -> new ProductNotFoundException(pid));

            int newStock = product.getStock() - item.getQuantity();
            if (newStock < 0) {
                throw new IllegalStateException("Insufficient stock for product: " + pid);
            }
            product.setStock(newStock);
            productRepo.save(product);
        }

        // Create and save the new order
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

    public void goodsIn(String productId, int amount) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        int newStock = product.getStock() + amount;
        product.setStock(newStock);
        productRepo.save(product);
    }

    public void goodsOut(String productId, int amount) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        int newStock = product.getStock() - amount;
        if (newStock < 0) {
            throw new IllegalStateException("Insufficient stock for product: " + productId);
        }
        product.setStock(newStock);
        productRepo.save(product);
    }

    public Cart reserveStockForCart(String cartId, List<CartItem> items) {
        // Decrement stock for each item
        for (CartItem item : items) {
            String productId = item.getProductId();
            Product product = productRepo.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));

            int remaining = product.getStock() - item.getQuantity();
            if (remaining < 0) {
                throw new IllegalStateException("Insufficient stock for product: " + productId);
            }
            product.setStock(remaining);
            productRepo.save(product);
        }

        // Build and save the shopping cart
        Cart cart = new Cart(cartId, items, Instant.now());
        return cartRepo.save(cart);
    }

    public BigDecimal calculateCartTotal(List<CartItem> items) {
        return items.stream()
                .map(item -> {
                    Product product = productRepo.findById(item.getProductId())
                            .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));
                    return product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
