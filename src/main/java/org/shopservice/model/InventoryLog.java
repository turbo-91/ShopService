package org.shopservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "inventory_logs")
public class InventoryLog {
    @Id
    private String id;
    private Instant timestamp;
    private int delta;
    private String sourceType;
    private String sourceId;

    public InventoryLog(int delta, String sourceType, String sourceId) {
        this.timestamp = Instant.now();
        this.delta = delta;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
    }
}