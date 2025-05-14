package org.example;

import java.util.List;

public interface OrderRepo {
    void addOrder(Order order);
    void removeOrder(Order order);
    Order getOrder(String id);
    List<Order> getOrders();
}
