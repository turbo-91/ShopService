package org.shopservice.repository;

import org.shopservice.model.InventoryLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface InventoryLogRepo extends MongoRepository<InventoryLog, String> {
    List<InventoryLog> findByTimestampBetween(Instant from, Instant to);
}