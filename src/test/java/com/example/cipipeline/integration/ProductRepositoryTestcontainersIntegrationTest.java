package com.example.cipipeline.integration;

import com.example.cipipeline.model.Product;
import com.example.cipipeline.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
@DisplayName("Integration Tests: ProductRepository with Testcontainers (PostgreSQL)")
class ProductRepositoryTestcontainersIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (postgres.isRunning()) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
            registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
            registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        }
    }

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void cleanUp() {
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("Should persist and retrieve product from database")
    void shouldPersistAndRetrieveProduct() {
        Product product = new Product("Ergonomic Chair", "Furniture", new BigDecimal("199.99"), 15);
        Product saved = productRepository.save(product);

        assertNotNull(saved.getId());

        Optional<Product> found = productRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Ergonomic Chair", found.get().getName());
        assertEquals("Furniture", found.get().getCategory());
        assertEquals(new BigDecimal("199.99"), found.get().getPrice());
        assertEquals(15, found.get().getStock());
    }

    @Test
    @DisplayName("Should find products by category")
    void shouldFindProductsByCategory() {
        productRepository.save(new Product("Desk Lamp", "Lighting", new BigDecimal("35.50"), 20));
        productRepository.save(new Product("Floor Lamp", "Lighting", new BigDecimal("75.00"), 10));
        productRepository.save(new Product("Standing Desk", "Furniture", new BigDecimal("350.00"), 5));

        List<Product> lightingProducts = productRepository.findByCategory("Lighting");

        assertEquals(2, lightingProducts.size());
        assertTrue(lightingProducts.stream().allMatch(p -> p.getCategory().equals("Lighting")));
    }

    @Test
    @DisplayName("Should find product by name")
    void shouldFindProductByName() {
        productRepository.save(new Product("Noise Cancelling Headphones", "Audio", new BigDecimal("149.99"), 30));

        Optional<Product> result = productRepository.findByName("Noise Cancelling Headphones");

        assertTrue(result.isPresent());
        assertEquals("Audio", result.get().getCategory());
        assertEquals(30, result.get().getStock());
    }

    @Test
    @DisplayName("Should update stock quantity in database")
    void shouldUpdateStockInDatabase() {
        Product product = productRepository.save(
                new Product("Webcam 4K", "Electronics", new BigDecimal("99.99"), 50));

        product.setStock(product.getStock() - 5);
        Product updated = productRepository.save(product);

        assertEquals(45, updated.getStock());
        Product retrieved = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(45, retrieved.getStock());
    }
}
