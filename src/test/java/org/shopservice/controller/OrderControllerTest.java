package org.shopservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shopservice.model.Order;
import org.shopservice.model.OrderItem;
import org.shopservice.model.enums.OrderStatus;
import org.shopservice.service.ShopService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerTest {

    private ShopService shopService;
    private OrderController orderController;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // GIVEN
        shopService = mock(ShopService.class);
        orderController = new OrderController(shopService);
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    void getAllOrders_ShouldReturnListOfOrders() throws Exception {
        // GIVEN
        Order o1 = new Order("order1", List.of(), OrderStatus.PROCESSING, Instant.now());
        Order o2 = new Order("order2", List.of(), OrderStatus.COMPLETED, Instant.now());
        when(shopService.getAllOrders()).thenReturn(List.of(o1, o2));

        // WHEN & THEN
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(o1, o2))));

        verify(shopService).getAllOrders();
    }

    @Test
    void getOrderById_ShouldReturnOrder_WhenExists() throws Exception {
        // GIVEN
        Order expected = new Order("order1", List.of(), OrderStatus.PROCESSING, Instant.now());
        when(shopService.getOrderById("order1")).thenReturn(expected);

        // WHEN & THEN
        mockMvc.perform(get("/orders/{id}", "order1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expected)));

        verify(shopService).getOrderById("order1");
    }

    @Test
    void getOrderById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        // GIVEN
        when(shopService.getOrderById("nope"))
                .thenThrow(new org.shopservice.exception.OrderNotFoundException("nope"));

        // WHEN & THEN
        mockMvc.perform(get("/orders/{id}", "nope"))
                .andExpect(status().isNotFound());

        verify(shopService).getOrderById("nope");
    }

    @Test
    void getOrdersByStatus_ShouldReturnFilteredList() throws Exception {
        // GIVEN
        Order o = new Order("order2", List.of(), OrderStatus.COMPLETED, Instant.now());
        when(shopService.getOrdersByStatus(OrderStatus.COMPLETED)).thenReturn(List.of(o));

        // WHEN & THEN
        mockMvc.perform(get("/orders")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(o))));

        verify(shopService).getOrdersByStatus(OrderStatus.COMPLETED);
    }

    @Test
    void placeOrder_ShouldReturnCreatedOrder() throws Exception {
        // GIVEN
        OrderItem item = new OrderItem(null, 3);
        Order input = new Order("newOrder", List.of(item), OrderStatus.PROCESSING, null);
        Order created = new Order("newOrder", List.of(item), OrderStatus.PROCESSING, Instant.now());
        when(shopService.placeOrder(eq("newOrder"), anyList(), eq(OrderStatus.PROCESSING)))
                .thenReturn(created);

        // WHEN & THEN
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(created)));

        verify(shopService).placeOrder(eq("newOrder"), anyList(), eq(OrderStatus.PROCESSING));
    }

    @Test
    void updateOrderStatus_ShouldReturnUpdatedOrder() throws Exception {
        // GIVEN
        Order updated = new Order("o1", List.of(), OrderStatus.COMPLETED, Instant.now());
        when(shopService.updateOrderStatus("o1", OrderStatus.COMPLETED)).thenReturn(updated);

        // WHEN & THEN
        mockMvc.perform(put("/orders/{id}/status", "o1")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(updated)));

        verify(shopService).updateOrderStatus("o1", OrderStatus.COMPLETED);
    }
}
