package org.shopservice.repository;

import org.shopservice.model.InventoryLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryLogRepo extends MongoRepository<InventoryLog, String> {
}