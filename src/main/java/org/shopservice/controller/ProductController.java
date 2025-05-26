package org.shopservice.controller;

import lombok.RequiredArgsConstructor;
import org.shopservice.exception.ProductNotFoundException;
import org.shopservice.model.Product;
import org.shopservice.repository.ProductRepo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepo productRepo;

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product saved = productRepo.save(product);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable String id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable String id,
                                 @RequestBody Product updatedProduct) {
        Product existing = productRepo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        updatedProduct.setId(existing.getId());
        return productRepo.save(updatedProduct);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable String id) {
        Product existing = productRepo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        productRepo.delete(existing);
    }
}
