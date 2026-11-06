package com.example.cipipeline.unit;

import com.example.cipipeline.dto.ProductRequest;
import com.example.cipipeline.dto.ProductResponse;
import com.example.cipipeline.exception.InvalidStockException;
import com.example.cipipeline.exception.ProductNotFoundException;
import com.example.cipipeline.model.Product;
import com.example.cipipeline.repository.ProductRepository;
import com.example.cipipeline.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests: ProductService Business Logic")
class ProductServiceUnitTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product(1L, "Mechanical Keyboard", "Electronics", new BigDecimal("89.99"), 25);
    }

    @Test
    @DisplayName("Should create product successfully")
    void shouldCreateProductSuccessfully() {
        ProductRequest request = new ProductRequest("Mechanical Keyboard", "Electronics", new BigDecimal("89.99"), 25);
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Mechanical Keyboard", response.getName());
        assertEquals("Electronics", response.getCategory());
        assertEquals(new BigDecimal("89.99"), response.getPrice());
        assertEquals(25, response.getStock());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should return product when found by ID")
    void shouldReturnProductWhenFoundById() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        ProductResponse response = productService.getProductById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Mechanical Keyboard", response.getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when ID does not exist")
    void shouldThrowExceptionWhenProductNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(999L));
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should return all products")
    void shouldReturnAllProducts() {
        Product secondProduct = new Product(2L, "Gaming Mouse", "Electronics", new BigDecimal("49.99"), 40);
        when(productRepository.findAll()).thenReturn(Arrays.asList(sampleProduct, secondProduct));

        List<ProductResponse> products = productService.getAllProducts();

        assertEquals(2, products.size());
        assertEquals("Mechanical Keyboard", products.get(0).getName());
        assertEquals("Gaming Mouse", products.get(1).getName());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should update stock successfully when sufficient quantity exists")
    void shouldUpdateStockSuccessfully() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductResponse response = productService.updateStock(1L, 10);

        assertEquals(35, sampleProduct.getStock());
        assertNotNull(response);
        verify(productRepository, times(1)).save(sampleProduct);
    }

    @Test
    @DisplayName("Should throw InvalidStockException when deducting more than available stock")
    void shouldThrowExceptionWhenDeductingTooMuchStock() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        InvalidStockException exception = assertThrows(InvalidStockException.class,
                () -> productService.updateStock(1L, -30));

        assertTrue(exception.getMessage().contains("Insufficient stock"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should delete product successfully when ID exists")
    void shouldDeleteProductSuccessfully() {
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when deleting non-existent product")
    void shouldThrowExceptionWhenDeletingNonExistentProduct() {
        when(productRepository.existsById(999L)).thenReturn(false);

        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(999L));
        verify(productRepository, never()).deleteById(anyLong());
    }
}
