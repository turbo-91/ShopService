package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class ShopService {
    private final OrderRepo orderRepo;
    private final ProductRepo productRepo;

    public ShopService(OrderRepo orderRepo, ProductRepo productRepo) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
    }

    public Order placeOrder(String id, List<OrderItem> items) {
        for (OrderItem item : items) {
            // getProduct will throw NoSuchElementException if not found
            productRepo.getProduct(item.product().id());
        }

        final Order newOrder = new Order(id, items);
        orderRepo.addOrder(newOrder);
        return newOrder;
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
        orderRepo.addOrder(new Order(order.id(), updatedItems));
    }

}
