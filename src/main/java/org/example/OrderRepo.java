package org.example;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepo extends MongoRepository<Order, String> {
    List<Order> findByOrderStatus(OrderStatus orderStatus);
    // Inherited methods:
    //   List<Order> findAll();                  // replaces getOrders()
    //   Optional<Order> findById(String id);    // replaces getOrder(String id)
    //   <S extends Order> S save(S entity);     // replaces addOrder(Order)
    //   void delete(Order entity);             // replaces removeOrder(Order)

    // Add custom query methods if needed, for example:
    // List<Order> findByOrderStatus(OrderStatus status);
    // List<Order> findByTimestampBetween(Instant from, Instant to);
}
