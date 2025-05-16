package org.example;

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

    public Order placeOrder(String id, List<OrderItem> items, OrderStatus orderStatus) {
        for (OrderItem item : items) {
            productRepo.getProductById(item.product().id())
                    .orElseThrow(() -> new ProductNotFoundException(item.product().id()));
        }

        Order newOrder = new Order(id, items, orderStatus);
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

    public void updateProductQuantity(String orderId, String productId, int newQuantity) {
        Order order = orderRepo.getOrder(orderId);
        List<OrderItem> updatedItems = new ArrayList<>();

        boolean found = false;

        for (OrderItem item : order.items()) {
            if (item.product().id().equals(productId)) {
                updatedItems.add(new OrderItem(item.product(), newQuantity));
                found = true;
            } else {
                updatedItems.add(item);
            }
        }

        if (!found) {
            throw new NoSuchElementException("Product not found in order: " + productId);
        }

        // Replace the order with the updated one
        orderRepo.removeOrder(order);
        orderRepo.addOrder(new Order(order.id(), updatedItems, order.orderStatus()));
    }

}
