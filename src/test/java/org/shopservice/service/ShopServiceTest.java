package org.shopservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shopservice.exception.InsufficientStockException;
import org.shopservice.exception.OrderNotFoundException;
import org.shopservice.exception.ProductNotFoundException;
import org.shopservice.model.*;
import org.shopservice.model.enums.OrderStatus;
import org.shopservice.repository.CartRepo;
import org.shopservice.repository.InventoryLogRepo;
import org.shopservice.repository.OrderRepo;
import org.shopservice.repository.ProductRepo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock private OrderRepo orderRepo;
    @Mock private ProductRepo productRepo;
    @Mock private CartRepo cartRepo;
    @Mock private InventoryLogRepo inventoryLogRepo;

    @InjectMocks private ShopService shopService;

    private Product product;
    private OrderItem orderItem;

    @BeforeEach
    void setUp() {
        product = new Product("P1", "Test", "Brand", "Desc", "Color", "Size", new BigDecimal("10.00"), 5);
        orderItem = new OrderItem(product, 2);
    }

    @Test
    void getAllOrders_shouldReturnListOfOrders() {
        // GIVEN
        List<Order> mockOrders = List.of(new Order("o1", List.of(orderItem), OrderStatus.PROCESSING, Instant.now()));
        when(orderRepo.findAll()).thenReturn(mockOrders);

        // WHEN
        List<Order> result = shopService.getAllOrders();

        // THEN
        assertEquals(mockOrders, result);
        verify(orderRepo).findAll();
    }

    @Test
    void getOrderById_shouldReturnOrder_whenExists() {
        // GIVEN
        Order order = new Order("o1", List.of(orderItem), OrderStatus.PROCESSING, Instant.now());
        when(orderRepo.findById("o1")).thenReturn(Optional.of(order));

        // WHEN
        Order result = shopService.getOrderById("o1");

        // THEN
        assertEquals(order, result);
        verify(orderRepo).findById("o1");
    }

    @Test
    void getOrderById_shouldThrowException_whenNotFound() {
        // GIVEN
        when(orderRepo.findById("x")).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(OrderNotFoundException.class, () -> shopService.getOrderById("x"));
        verify(orderRepo).findById("x");
    }

    @Test
    void placeOrder_shouldSaveOrderAndReduceStock() {
        // GIVEN
        when(productRepo.findById("P1")).thenReturn(Optional.of(product));
        when(orderRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        Order result = shopService.placeOrder("o1", List.of(orderItem), OrderStatus.PROCESSING);

        // THEN
        assertEquals("o1", result.getId());
        assertEquals(3, product.getStock());
        verify(inventoryLogRepo).save(any());
        verify(orderRepo).save(any());
    }

    @Test
    void placeOrder_shouldThrow_whenInsufficientStock() {
        // GIVEN
        product.setStock(1);
        when(productRepo.findById("P1")).thenReturn(Optional.of(product));

        // WHEN & THEN
        assertThrows(IllegalStateException.class,
                () -> shopService.placeOrder("o1", List.of(orderItem), OrderStatus.PROCESSING));
        verify(productRepo).findById("P1");
    }

    @Test
    void updateOrderStatus_shouldUpdateStatus_whenExists() {
        // GIVEN
        Order existing = new Order("o1", List.of(orderItem), OrderStatus.PROCESSING, Instant.now());
        when(orderRepo.findById("o1")).thenReturn(Optional.of(existing));
        when(orderRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        Order result = shopService.updateOrderStatus("o1", OrderStatus.COMPLETED);

        // THEN
        assertEquals(OrderStatus.COMPLETED, result.getStatus());
        verify(orderRepo).findById("o1");
        verify(orderRepo).save(any());
    }

    @Test
    void updateOrderStatus_shouldThrow_whenNotFound() {
        // GIVEN
        when(orderRepo.findById("x")).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(NoSuchElementException.class,
                () -> shopService.updateOrderStatus("x", OrderStatus.COMPLETED));
    }

    @Test
    void cancelOrder_shouldRestockAndLog() {
        // GIVEN
        OrderItem item = new OrderItem(product, 2);
        Order existing = new Order("o1", List.of(item), OrderStatus.PROCESSING, Instant.now());
        when(orderRepo.findById("o1")).thenReturn(Optional.of(existing));
        when(productRepo.findById("P1")).thenReturn(Optional.of(product));
        when(productRepo.save(any())).thenReturn(product);
        when(orderRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        Order canceled = shopService.cancelOrder("o1");

        // THEN
        assertEquals(OrderStatus.CANCELED, canceled.getStatus());
        assertEquals(7, product.getStock());
        verify(inventoryLogRepo).save(any());
    }

    @Test
    void refundOrder_shouldRestockAndLog_whenCompleted() {
        // GIVEN
        Order existing = new Order("o1", List.of(orderItem), OrderStatus.COMPLETED, Instant.now());
        when(orderRepo.findById("o1")).thenReturn(Optional.of(existing));
        when(productRepo.findById("P1")).thenReturn(Optional.of(product));
        when(productRepo.save(any())).thenReturn(product);
        when(orderRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        Order refunded = shopService.refundOrder("o1");

        // THEN
        assertEquals(OrderStatus.REFUNDED, refunded.getStatus());
        verify(inventoryLogRepo, times(1)).save(any());
    }

    @Test
    void refundOrder_shouldThrow_whenNotCompleted() {
        // GIVEN
        Order existing = new Order("o1", List.of(orderItem), OrderStatus.PROCESSING, Instant.now());
        when(orderRepo.findById("o1")).thenReturn(Optional.of(existing));

        // WHEN & THEN
        assertThrows(IllegalStateException.class,
                () -> shopService.refundOrder("o1"));
    }

    @Test
    void goodsIn_shouldIncreaseStockAndLog() {
        // GIVEN
        when(productRepo.findById("P1")).thenReturn(Optional.of(product));
        when(productRepo.save(any())).thenReturn(product);

        // WHEN
        shopService.goodsIn("P1", 5);

        // THEN
        assertEquals(10, product.getStock());
        verify(inventoryLogRepo).save(argThat(log -> log.getDelta() == 5));
    }

    @Test
    void goodsOut_shouldDecreaseStockAndLog() {
        // GIVEN
        when(productRepo.findById("P1")).thenReturn(Optional.of(product));
        when(productRepo.save(any())).thenReturn(product);

        // WHEN
        shopService.goodsOut("P1", 3);

        // THEN
        assertEquals(2, product.getStock());
        verify(inventoryLogRepo).save(argThat(log -> log.getDelta() == -3));
    }

    @Test
    void goodsOut_shouldThrow_whenInsufficient() {
        // GIVEN
        product.setStock(1);
        when(productRepo.findById("P1")).thenReturn(Optional.of(product));

        // WHEN & THEN
        assertThrows(InsufficientStockException.class,
                () -> shopService.goodsOut("P1", 5));
    }

    @Test
    void releaseReservedStock_shouldIncreaseStockAndLog() {
        // GIVEN
        when(productRepo.findById("P1")).thenReturn(Optional.of(product));
        when(productRepo.save(any())).thenReturn(product);

        // WHEN
        shopService.releaseReservedStock("P1", 2);

        // THEN
        assertEquals(7, product.getStock());
        verify(inventoryLogRepo).save(argThat(log -> log.getDelta() == 2));
    }

    @Test
    void reserveStockForCart_shouldReserveAndLog() {
        // GIVEN
        CartItem cartItem = new CartItem("P1", 3);
        when(productRepo.findById("P1")).thenReturn(Optional.of(product));
        when(cartRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        Cart cart = shopService.reserveStockForCart("c1", List.of(cartItem));

        // THEN
        assertEquals(2, product.getStock());
        verify(inventoryLogRepo).save(argThat(log -> log.getDelta() == -3));
        assertEquals("c1", cart.getId());
    }

    @Test
    void reserveStockForCart_shouldThrow_whenInsufficient() {
        // GIVEN
        CartItem cartItem = new CartItem("P1", 3);
        product.setStock(2);
        when(productRepo.findById("P1")).thenReturn(Optional.of(product));

        // WHEN & THEN
        assertThrows(IllegalStateException.class,
                () -> shopService.reserveStockForCart("c2", List.of(cartItem)));
    }

    @Test
    void calculateCartTotal_shouldComputeCorrectly() {
        // GIVEN
        CartItem ci = new CartItem("P1", 2);
        when(productRepo.findById("P1")).thenReturn(Optional.of(product));

        // WHEN
        BigDecimal total = shopService.calculateCartTotal(List.of(ci));

        // THEN
        assertEquals(new BigDecimal("20.00"), total);
    }

    @Test
    void searchProducts_shouldReturnMatchingList() {
        // GIVEN
        List<Product> products = List.of(product);
        when(productRepo.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(any(), any(), any(), any(), any()))
                .thenReturn(products);

        // WHEN
        List<Product> result = shopService.searchProducts("Test");

        // THEN
        assertEquals(products, result);
    }

}
