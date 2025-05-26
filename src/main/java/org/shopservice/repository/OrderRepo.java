package org.shopservice.repository;

import org.shopservice.model.Order;
import org.shopservice.model.enums.OrderStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepo extends MongoRepository<Order, String> {
    List<Order> findByStatus(OrderStatus status);
}
