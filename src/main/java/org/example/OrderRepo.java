package org.example;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepo extends MongoRepository<Order, String> {
    List<Order> findByOrderStatus(OrderStatus orderStatus);
}
