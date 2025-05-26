package org.shopservice.controller;

import lombok.RequiredArgsConstructor;
import org.shopservice.service.ShopService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final ShopService shopService;

    @PostMapping("/in")
    public ResponseEntity<Void> goodsIn(@RequestParam String productId,
                                        @RequestParam int amount) {
        shopService.goodsIn(productId, amount);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/out")
    public ResponseEntity<Void> goodsOut(@RequestParam String productId,
                                         @RequestParam int amount) {
        shopService.goodsOut(productId, amount);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
