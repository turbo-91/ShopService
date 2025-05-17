package org.example;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepo extends MongoRepository<Cart, String> {
}