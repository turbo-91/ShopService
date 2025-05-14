package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class OrderListRepo implements OrderRepo {
    List<Order> orders = new ArrayList<>();

    public Order getOrder(String id){
        for (Order order : orders) {
            if(Objects.equals(order.id(), id)) {
                return order;
            }
        }
        throw new NoSuchElementException("No order with id " + id);
    }

    @Override
    public List<Order> getOrders() {
        return orders;
    }

    public void addOrder(Order order){
        orders.add(order);
    }

    public void removeOrder(Order order) {
        orders.remove(order);
    }

}
