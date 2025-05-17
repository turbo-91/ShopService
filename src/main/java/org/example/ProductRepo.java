package org.example;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ProductRepo extends MongoRepository<Product, String> {
    Optional<Object> getProductById(String id);
    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase
            (String nameKw,
             String descKw,
             String brandKw,
             String colorKw,
             String sizeKw);
}
