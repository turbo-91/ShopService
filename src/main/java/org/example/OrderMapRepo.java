package org.example;

import java.util.*;

public class OrderMapRepo implements OrderRepo {
    private final Map<String, Order> orders = new HashMap<>();

    @Override
    public void addOrder(Order order) {
        orders.put(order.id(), order);
    }

    @Override
    public void removeOrder(Order order) {
        orders.remove(order.id());
    }

    @Override
    public Order getOrder(String id) {
        Order order = orders.get(id);
        if (order == null) {
            throw new NoSuchElementException("No order with id " + id);
        }
        return order;
    }

    @Override
    public List<Order> getOrders() {
        return new ArrayList<>(orders.values());
    }
}
