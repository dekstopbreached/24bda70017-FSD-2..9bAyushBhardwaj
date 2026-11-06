package com.example.cipipeline.integration;

import com.example.cipipeline.model.Product;
import com.example.cipipeline.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Integration Tests: ProductRepository Data JPA Layer")
class ProductRepositoryIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("Should persist and find product by ID")
    void shouldPersistAndFindById() {
        Product product = new Product("Mechanical Keyboard", "Peripherals", new BigDecimal("89.99"), 20);
        Product saved = productRepository.save(product);

        assertNotNull(saved.getId());

        Optional<Product> found = productRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Mechanical Keyboard", found.get().getName());
        assertEquals("Peripherals", found.get().getCategory());
        assertEquals(new BigDecimal("89.99"), found.get().getPrice());
        assertEquals(20, found.get().getStock());
    }

    @Test
    @DisplayName("Should find products by category")
    void shouldFindProductsByCategory() {
        productRepository.save(new Product("Keyboard", "Accessories", new BigDecimal("45.00"), 10));
        productRepository.save(new Product("Mouse Pad", "Accessories", new BigDecimal("15.00"), 50));
        productRepository.save(new Product("Desk", "Furniture", new BigDecimal("250.00"), 5));

        List<Product> accessories = productRepository.findByCategory("Accessories");

        assertEquals(2, accessories.size());
        assertTrue(accessories.stream().allMatch(p -> p.getCategory().equals("Accessories")));
    }

    @Test
    @DisplayName("Should find product by name")
    void shouldFindProductByName() {
        productRepository.save(new Product("Smart Watch", "Wearables", new BigDecimal("199.99"), 15));

        Optional<Product> found = productRepository.findByName("Smart Watch");

        assertTrue(found.isPresent());
        assertEquals("Wearables", found.get().getCategory());
        assertEquals(15, found.get().getStock());
    }

    @Test
    @DisplayName("Should update stock quantity")
    void shouldUpdateStockQuantity() {
        Product product = productRepository.save(new Product("USB-C Hub", "Cables", new BigDecimal("29.99"), 30));

        product.setStock(product.getStock() - 5);
        Product updated = productRepository.save(product);

        assertEquals(25, updated.getStock());
    }

    @Test
    @DisplayName("Should delete product by ID")
    void shouldDeleteProductById() {
        Product product = productRepository.save(new Product("Webcam", "Video", new BigDecimal("59.99"), 12));

        productRepository.deleteById(product.getId());

        assertFalse(productRepository.findById(product.getId()).isPresent());
    }
}
