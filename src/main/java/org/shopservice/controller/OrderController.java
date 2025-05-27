package org.shopservice.controller;

import lombok.RequiredArgsConstructor;
import org.shopservice.exception.OrderNotFoundException;
import org.shopservice.model.Order;
import org.shopservice.model.OrderItem;
import org.shopservice.model.enums.OrderStatus;
import org.shopservice.service.ShopService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final ShopService shopService;

    // POST /orders — Place a new order
    @PostMapping
    public ResponseEntity<Order> placeOrder(@RequestParam String id,
                                            @RequestBody List<OrderItem> items,
                                            @RequestParam OrderStatus status) {
        Order placed = shopService.placeOrder(id, items, status);
        return new ResponseEntity<>(placed, HttpStatus.CREATED);
    }

    // GET /orders/{id} — Get order by ID
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable String id) {
        Order order = shopService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    // GET /orders?status=... — Get orders by status or all
    @GetMapping
    public List<Order> getOrdersByStatus(@RequestParam(required = false) OrderStatus status) {
        if (status != null) {
            return shopService.getOrdersByStatus(status);
        }
        return shopService.getAllOrders();
    }

    // PUT /orders/{id}/status — Update order status
    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable String id,
                                                   @RequestParam OrderStatus status) {
        Order updated = shopService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    // PUT /orders/{id}/items — Update quantity of an item in the order
    @PutMapping("/{id}/items")
    public ResponseEntity<Order> updateItemQuantity(@PathVariable String id,
                                                    @RequestParam String productId,
                                                    @RequestParam int quantity) {
        Order updated = shopService.updateOrderItemQuantity(id, productId, quantity);
        return ResponseEntity.ok(updated);
    }

    // DELETE /orders/{id} — Cancel the order
    @DeleteMapping("/{id}")
    public ResponseEntity<Order> cancelOrder(@PathVariable String id) {
        Order canceled = shopService.cancelOrder(id);
        return ResponseEntity.ok(canceled);
    }
}
