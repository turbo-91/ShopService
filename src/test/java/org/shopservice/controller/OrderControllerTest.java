package org.shopservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shopservice.model.Order;
import org.shopservice.model.OrderItem;
import org.shopservice.model.Product;
import org.shopservice.model.enums.OrderStatus;
import org.shopservice.repository.OrderRepo;
import org.shopservice.repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
@TestPropertySource(properties = {
        // point at an in-memory test database
        "spring.data.mongodb.database=shop-service-test"
})
class OrderControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private OrderRepo orderRepository;

    @Autowired
    private ProductRepo productRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    void beforeEach() {
        // completely wipe out between tests
        orderRepository.deleteAll();
        productRepository.deleteAll();
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    private Product sampleProduct() {
        return new Product(
                "prod-1",
                "Gizmo",
                "Acme",
                "A very useful gizmo",
                "blue",
                "L",
                BigDecimal.valueOf(19.95),
                42
        );
    }

    private OrderItem sampleItem() {
        return new OrderItem(sampleProduct(), 3);
    }

    @Test
    void getAllOrders_whenNoneExist_returnsEmptyList() throws Exception {
        mvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getAllOrders_whenSomeExist_returnsThem() throws Exception {
        // seed two products & orders
        Product p1 = new Product("P1", "T-Shirt", "ACME Apparel", "Crew neck", "Red", "M", BigDecimal.valueOf(19.99), 50);
        Product p2 = new Product("P2", "Jeans",   "DenimCo",     "Slim-fit", "Blue","32", BigDecimal.valueOf(49.99),30);
        productRepository.saveAll(List.of(p1, p2));

        Order o1 = new Order("order1", List.of(new OrderItem(p1, 2)), OrderStatus.CANCELED, Instant.now());
        Order o2 = new Order("order2", List.of(
                new OrderItem(p1, 1),
                new OrderItem(p2, 5)
        ), OrderStatus.REFUNDED, Instant.now());
        orderRepository.saveAll(List.of(o1, o2));

        mvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].id", is("order1")))
                .andExpect(jsonPath("$[1].id", is("order2")));
    }

    @Test
    void getOrdersByStatus_filtersByStatus() throws Exception {
        productRepository.save(sampleProduct());

        Order a = new Order("a", List.of(sampleItem()), OrderStatus.PROCESSING, Instant.now());
        Order b = new Order("b", List.of(sampleItem()), OrderStatus.CANCELED,   Instant.now());
        orderRepository.saveAll(List.of(a, b));

        mvc.perform(get("/orders").param("status", "CANCELED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is("b")));
    }

    @Test
    void getOrderById_whenExists_returnsIt() throws Exception {
        productRepository.save(sampleProduct());
        Order x = new Order("X", List.of(sampleItem()), OrderStatus.PROCESSING, Instant.now());
        orderRepository.save(x);

        mvc.perform(get("/orders/{id}", "X"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("X")))
                .andExpect(jsonPath("$.status", is("PROCESSING")));
    }

    @Test
    void getOrderById_whenNotExists_returns404() throws Exception {
        mvc.perform(get("/orders/{id}", "no-such"))
                .andExpect(status().isNotFound());
    }

    @Test
    void placeOrder_createsAndReturnsNewOrder() throws Exception {
        // must seed product so placeOrder can find it
        productRepository.save(sampleProduct());

        List<OrderItem> body = List.of(sampleItem());
        mvc.perform(post("/orders")
                        .param("id", "new-order")
                        .param("status", "PROCESSING")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("new-order")))
                .andExpect(jsonPath("$.status", is("PROCESSING")))
                .andExpect(jsonPath("$.items[0].quantity", is(3)))
                .andExpect(jsonPath("$.items[0].product.id", is("prod-1")));

        // verify in DB
        assert(orderRepository.existsById("new-order"));
    }

    @Test
    void updateOrderStatus_changesStatus() throws Exception {
        productRepository.save(sampleProduct());
        Order o = new Order("u1", List.of(sampleItem()), OrderStatus.PROCESSING, Instant.now());
        orderRepository.save(o);

        mvc.perform(put("/orders/{id}/status", "u1")
                        .param("status", "COMPLETED")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    @Test
    void updateItemQuantity_changesQuantity() throws Exception {
        productRepository.save(sampleProduct());
        Order o = new Order("q1", List.of(sampleItem()), OrderStatus.PROCESSING, Instant.now());
        orderRepository.save(o);

        mvc.perform(put("/orders/{id}/items", "q1")
                        .param("productId", "prod-1")
                        .param("quantity", "7")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity", is(7)));
    }

    @Test
    void cancelOrder_marksCanceledAndRestoresStock() throws Exception {
        // seed product
        productRepository.save(sampleProduct());

        // place an order first
        Order o = new Order("c1", List.of(sampleItem()), OrderStatus.PROCESSING, Instant.now());
        orderRepository.save(o);

        mvc.perform(delete("/orders/{id}", "c1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELED")));

        // stock should have been restored from 42 → 42 + 3 = 45
        Product refreshed = productRepository.findById("prod-1").orElseThrow();
        assertEquals(45, refreshed.getStock());
    }
}
