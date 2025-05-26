package org.shopservice.service;

import lombok.RequiredArgsConstructor;
import org.shopservice.exception.InsufficientStockException;
import org.shopservice.exception.OrderNotFoundException;
import org.shopservice.exception.ProductNotFoundException;
import org.shopservice.model.*;
import org.shopservice.model.enums.OrderStatus;
import org.shopservice.repository.CartRepo;
import org.shopservice.repository.InventoryLogRepo;
import org.shopservice.repository.OrderRepo;
import org.shopservice.repository.ProductRepo;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final InventoryLogRepo inventoryLogRepo;

    private static final Logger logger = LoggerFactory.getLogger(ShopService.class);

    // Order Management

    public List<Order> getAllOrders() {
        return orderRepo.findAll();
    }

    public Order getOrderById(String orderId) {
        logger.info("Fetching order with ID: {}", orderId);
        return orderRepo.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    public List<Order> getOrdersByStatus(OrderStatus orderStatus) {
        return orderRepo.findByStatus(orderStatus);
    }

    public Order placeOrder(String id,
                            List<OrderItem> items,
                            OrderStatus orderStatus) {
        logger.info("Placing order {} with {} items", id, items.size());
        // Validate each product exists and decrement its stock
        for (OrderItem item : items) {
            String pid = item.getProduct().getId();
            Product product = productRepo.findById(pid)
                    .orElseThrow(() -> new ProductNotFoundException(pid));

            int newStock = product.getStock() - item.getQuantity();
            if (newStock < 0) {
                logger.error("Insufficient stock (have={}, need={}) for product {}",
                        product.getStock(), item.getQuantity(), pid);
                throw new IllegalStateException("Insufficient stock for product: " + pid);
            }
            product.setStock(newStock);
            productRepo.save(product);
            inventoryLogRepo.save(InventoryLog.builder()
                    .delta(-item.getQuantity())
                    .sourceType("PlaceOrder")
                    .sourceId(id)
                    .timestamp(Instant.now())
                    .build());
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
        logger.debug("Updating order {} status → {}", orderId, newStatus);
        Order updated = orderRepo.save(existing.withOrderStatus(newStatus));
        logger.info("Order {} status updated to {}", orderId, newStatus);
        return updated;
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
            logger.warn("Tried to update quantity for product {} in order {} but item not found",
                    productId, orderId);
            throw new NoSuchElementException(
                    "Product not found in order: " + productId
            );
        }

        Order updatedOrder = existing.withItems(updatedItems);
        Order saved = orderRepo.save(updatedOrder);
        logger.info("Order {} item {} quantity set to {}",
                orderId, productId, newQuantity);
        return saved;
    }

    public Order cancelOrder(String orderId) {
        logger.info("Cancelling order {}", orderId);
        Order existing = orderRepo.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));

        // Restock each item
        for (OrderItem item : existing.getItems()) {
            String pid = item.getProduct().getId();
            Product product = productRepo.findById(pid)
                    .orElseThrow(() -> new ProductNotFoundException(pid));
            product.setStock(product.getStock() + item.getQuantity());
            productRepo.save(product);
            logger.debug("Restocked {} units of product {}", item.getQuantity(), pid);
            inventoryLogRepo.save(InventoryLog.builder()
                    .delta(item.getQuantity())
                    .sourceType("CancelOrder")
                    .sourceId(orderId)
                    .timestamp(Instant.now())
                    .build());
        }

        // Mark order as canceled
        Order canceled = existing.withOrderStatus(OrderStatus.CANCELED);
        Order saved = orderRepo.save(canceled);
        logger.info("Order {} marked as CANCELED", orderId);
        return saved;
    }

    public Order refundOrder(String orderId) {
        // 1) Fetch order or fail
        Order existing = orderRepo.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));

        // 2) Validate state
        if (existing.getStatus() == OrderStatus.REFUNDED) {
            throw new IllegalStateException("Order already refunded: " + orderId);
        }
        if (existing.getStatus() != OrderStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Only completed orders can be refunded. Current status: " + existing.getStatus()
            );
        }

        // 3) Restock products
        for (OrderItem item : existing.getItems()) {
            String productId = item.getProduct().getId();
            Product product = productRepo.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));
            product.setStock(product.getStock() + item.getQuantity());
            productRepo.save(product);
            inventoryLogRepo.save(InventoryLog.builder()
                    .delta(item.getQuantity())
                    .sourceType("RefundOrder")
                    .sourceId(orderId)
                    .timestamp(Instant.now())
                    .build());
        }

        // 4) Mark refunded and save
        Order refunded = existing.withOrderStatus(OrderStatus.REFUNDED);
        Order saved    = orderRepo.save(refunded);

        // 5) Log it
        logger.info("Processed refund for order {}", orderId);

        return saved;
    }

    // STOCK MANAGEMENT

    public void goodsIn(String productId, int amount) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        int newStock = product.getStock() + amount;
        product.setStock(newStock);
        logger.info("Increasing stock for product {} by {}", productId, amount);
        productRepo.save(product);
        inventoryLogRepo.save(InventoryLog.builder()
                .delta(amount)
                .sourceType("GoodsIn")
                .sourceId(productId)
                .timestamp(Instant.now())
                .build());
    }

    public void goodsOut(String productId, int amount) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        int newStock = product.getStock() - amount;
        if (newStock < 0) {
            logger.error("Cannot remove {} units from product {} – only {} in stock",
                    amount, productId, product.getStock());
            throw new InsufficientStockException("Insufficient stock for product: " + productId);
        }
        product.setStock(newStock);
        logger.info("Decreasing stock for product {} by {}", productId, amount);
        productRepo.save(product);
        inventoryLogRepo.save(InventoryLog.builder()
                .delta(-amount)
                .sourceType("GoodsOut")
                .sourceId(productId)
                .timestamp(Instant.now())
                .build());
    }

    public void releaseReservedStock(String productId, int amount) {
        logger.info("Releasing {} units back to stock for product {}", amount, productId);
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.setStock(product.getStock() + amount);
        productRepo.save(product);
        inventoryLogRepo.save(InventoryLog.builder()
                .delta(amount)
                .sourceType("ReleaseReservedStock")
                .sourceId(productId)
                .timestamp(Instant.now())
                .build());
    }

    // CART OPERATIONS

    public Cart reserveStockForCart(String cartId, List<CartItem> items) {
        logger.info("Reserving stock for cart {} ({} items)", cartId, items.size());
        // Decrement stock for each item
        for (CartItem item : items) {
            String productId = item.getProductId();
            Product product = productRepo.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));

            int remaining = product.getStock() - item.getQuantity();
            if (remaining < 0) {
                logger.warn("Insufficient stock for reservation: product {} needed {}, have {}",
                        productId, item.getQuantity(), product.getStock());
                throw new IllegalStateException("Insufficient stock for product: " + productId);
            }
            product.setStock(remaining);
            productRepo.save(product);
            inventoryLogRepo.save(InventoryLog.builder()
                    .delta(-item.getQuantity())
                    .sourceType("ReserveCart")
                    .sourceId(cartId)
                    .timestamp(Instant.now())
                    .build());
        }

        // Build and save the shopping cart
        Cart cart = new Cart(cartId, items, Instant.now());
        Cart saved = cartRepo.save(cart);
        logger.info("Stock reserved for cart {}; created at {}", saved.getId(), saved.getCreatedAt());
        return saved;
    }

    public BigDecimal calculateCartTotal(List<CartItem> items) {
        logger.debug("Calculating cart total for {} items", items.size());

        BigDecimal total = items.stream()
                .map(item -> {
                    Product product = productRepo.findById(item.getProductId())
                            .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));
                    BigDecimal lineTotal = product.getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    logger.debug("Line item: productId={}, price={}, quantity={}, lineTotal={}",
                            item.getProductId(),
                            product.getPrice(),
                            item.getQuantity(),
                            lineTotal);
                    return lineTotal;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        logger.info("Cart total computed: {}", total);
        return total;
    }

    // PRODUCT SEARCH

    public List<Product> searchProducts(String keyword) {
        String kw = keyword.trim();
        logger.info("Searching products with keyword='{}'", kw);

        List<Product> results = productRepo
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(kw, kw, kw, kw, kw);

        logger.info("Found {} products matching '{}'", results.size(), kw);
        return results;
    }

}
