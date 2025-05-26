package org.shopservice.repository;

import org.shopservice.model.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepo extends MongoRepository<Cart, String> {
}