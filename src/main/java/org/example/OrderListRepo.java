package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class OrderListRepo {
    List<Order> orders = new ArrayList<>();

    public List<Order> getOrders(String id) {
        return orders;
    }

    public Order getOrder(String id){
        for (Order order : orders) {
            if(Objects.equals(order.id(), id)) {
                return order;
            }
        }
        throw new NoSuchElementException("No order with id " + id);
    }

    public void addOrder(Order order){
        orders.add(order);
    }

    public void removeOrder(Order order) {
        orders.remove(order);
    }

}
