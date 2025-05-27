package org.shopservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
class ProductControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDb() {
        productRepo.deleteAll();
    }

    @Test
    void getAllProducts_shouldReturnEmptyList_whenRepositoryIsEmpty() throws Exception {
        mvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getAllProducts_shouldReturnListWithOneProduct_whenOneSaved() throws Exception {
        Product p = new Product(
                "p1",
                "Widget",
                "Acme",
                "Standard widget",
                "red",
                "M",
                BigDecimal.valueOf(9.99),
                100
        );
        productRepo.save(p);

        mvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(p))));
    }

    @Test
    void createProduct_shouldReturnCreatedProduct_whenValidRequest() throws Exception {
        Product toCreate = new Product(
                "p2",
                "Gizmo",
                "Acme",
                "Useful gizmo",
                "blue",
                "L",
                BigDecimal.valueOf(19.95),
                50
        );
        String json = objectMapper.writeValueAsString(toCreate);

        mvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().json(json));

        assertTrue(productRepo.existsById("p2"));
    }

    @Test
    void getProductById_shouldReturnProduct_whenExists() throws Exception {
        Product p = new Product(
                "p3",
                "Thingamajig",
                "BrandX",
                "Fancy thingamajig",
                "green",
                "S",
                BigDecimal.valueOf(14.50),
                20
        );
        productRepo.save(p);

        mvc.perform(get("/products/{id}", "p3"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(p)));
    }

    @Test
    void getProductById_shouldReturnNotFound_whenNotExists() throws Exception {
        mvc.perform(get("/products/{id}", "missing"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product with id 'missing' not found."));
    }

    @Test
    void updateProduct_shouldReturnUpdatedProduct_whenExists() throws Exception {
        Product original = new Product(
                "p4",
                "OldName",
                "OldBrand",
                "Old desc",
                "black",
                "XL",
                BigDecimal.valueOf(29.99),
                5
        );
        productRepo.save(original);

        Product updated = new Product(
                "p4",
                "NewName",
                "NewBrand",
                "New desc",
                "white",
                "L",
                BigDecimal.valueOf(39.99),
                8
        );
        String updatedJson = objectMapper.writeValueAsString(updated);

        mvc.perform(put("/products/{id}", "p4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk())
                .andExpect(content().json(updatedJson));

        var fromDb = productRepo.findById("p4").orElseThrow();
        assertEquals("NewName", fromDb.getName());
        assertEquals("NewBrand", fromDb.getBrand());
        assertEquals(8, fromDb.getStock());
    }

    @Test
    void updateProduct_shouldReturnNotFound_whenNotExists() throws Exception {
        Product dummy = new Product(
                "p5",
                "Whatever",
                "X",
                "Desc",
                "yellow",
                "S",
                BigDecimal.valueOf(1.23),
                1
        );
        String json = objectMapper.writeValueAsString(dummy);

        mvc.perform(put("/products/{id}", "p5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product with id 'p5' not found."));
    }

    @Test
    void deleteProduct_shouldReturnNoContent_whenExists() throws Exception {
        Product p = new Product(
                "p6",
                "DeleteMe",
                "BrandDel",
                "To be deleted",
                "pink",
                "XS",
                BigDecimal.valueOf(5.55),
                3
        );
        productRepo.save(p);

        mvc.perform(delete("/products/{id}", "p6"))
                .andExpect(status().isNoContent());

        assertFalse(productRepo.existsById("p6"));
    }

    @Test
    void deleteProduct_shouldReturnNotFound_whenNotExists() throws Exception {
        mvc.perform(delete("/products/{id}", "gone"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product with id 'gone' not found."));
    }
}
