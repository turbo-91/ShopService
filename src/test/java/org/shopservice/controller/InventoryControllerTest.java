package org.shopservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shopservice.exception.ProductNotFoundException;
import org.shopservice.model.Product;
import org.shopservice.repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class InventoryControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ProductRepo productRepo;

    @BeforeEach
    void cleanDb() {
        productRepo.deleteAll();
    }

    @Test
    @DirtiesContext
    void goodsIn_shouldIncreaseStock_whenProductExists() throws Exception {
        // arrange: create product with initial stock 10
        Product p = new Product(
                "p1",
                "Widget",
                "Acme",
                "Standard widget",
                "red",
                "M",
                BigDecimal.valueOf(9.99),
                10
        );
        productRepo.save(p);

        // act: bring in 5 more
        mvc.perform(post("/inventory/in")
                        .param("productId", "p1")
                        .param("amount", "5")
                        .with(csrf())
                )
                // assert: 204 NO CONTENT
                .andExpect(status().isNoContent());

        // verify: stock is now 15
        Product refreshed = productRepo.findById("p1")
                .orElseThrow();
        assertThat(refreshed.getStock()).isEqualTo(15);
    }

    @Test
    @DirtiesContext
    void goodsOut_shouldDecreaseStock_whenSufficientStock() throws Exception {
        // arrange: create product with initial stock 20
        Product p = new Product(
                "p2",
                "Gadget",
                "Acme",
                "Fancy gadget",
                "blue",
                "L",
                BigDecimal.valueOf(19.95),
                20
        );
        productRepo.save(p);

        // act: take out 7
        mvc.perform(post("/inventory/out")
                        .param("productId", "p2")
                        .param("amount", "7")
                        .with(csrf())
                )
                // assert: 204 NO CONTENT
                .andExpect(status().isNoContent());

        // verify: stock is now 13
        Product refreshed = productRepo.findById("p2")
                .orElseThrow();
        assertThat(refreshed.getStock()).isEqualTo(13);
    }

    @Test
    @DirtiesContext
    void goodsIn_shouldReturnNotFound_whenProductDoesNotExist() throws Exception {
        mvc.perform(post("/inventory/in")
                        .param("productId", "missing")
                        .param("amount", "3")
                        .with(csrf())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DirtiesContext
    void goodsOut_shouldReturnNotFound_whenProductDoesNotExist() throws Exception {
        mvc.perform(post("/inventory/out")
                        .param("productId", "missing")
                        .param("amount", "3")
                        .with(csrf())
                )
                .andExpect(status().isNotFound());
    }
}
