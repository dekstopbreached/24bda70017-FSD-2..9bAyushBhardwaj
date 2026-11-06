package com.example.cipipeline.controller;

import com.example.cipipeline.dto.ProductRequest;
import com.example.cipipeline.dto.ProductResponse;
import com.example.cipipeline.dto.StockUpdateRequest;
import com.example.cipipeline.exception.GlobalExceptionHandler;
import com.example.cipipeline.exception.ProductNotFoundException;
import com.example.cipipeline.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("Integration / API Tests: ProductController with MockMvc")
class ProductControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    @DisplayName("POST /api/products - Should create product and return 201 Created")
    void shouldCreateProduct() throws Exception {
        ProductRequest request = new ProductRequest("Wireless Mouse", "Electronics", new BigDecimal("29.99"), 50);
        ProductResponse response = new ProductResponse(1L, "Wireless Mouse", "Electronics", new BigDecimal("29.99"), 50);

        when(productService.createProduct(any(ProductRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Wireless Mouse"))
                .andExpect(jsonPath("$.category").value("Electronics"))
                .andExpect(jsonPath("$.price").value(29.99))
                .andExpect(jsonPath("$.stock").value(50));
    }

    @Test
    @DisplayName("POST /api/products - Should return 400 Bad Request when validation fails")
    void shouldReturnBadRequestOnValidationFailure() throws Exception {
        ProductRequest invalidRequest = new ProductRequest("", "", new BigDecimal("-5.00"), -1);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.price").exists())
                .andExpect(jsonPath("$.fieldErrors.stock").exists());
    }

    @Test
    @DisplayName("GET /api/products/{id} - Should return 200 OK when product exists")
    void shouldReturnProductById() throws Exception {
        ProductResponse response = new ProductResponse(1L, "Wireless Mouse", "Electronics", new BigDecimal("29.99"), 50);

        when(productService.getProductById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/products/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Wireless Mouse"))
                .andExpect(jsonPath("$.price").value(29.99));
    }

    @Test
    @DisplayName("GET /api/products/{id} - Should return 404 Not Found when product does not exist")
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        when(productService.getProductById(999L)).thenThrow(new ProductNotFoundException(999L));

        mockMvc.perform(get("/api/products/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Product not found with id: 999"));
    }

    @Test
    @DisplayName("GET /api/products - Should return list of products")
    void shouldReturnAllProducts() throws Exception {
        ProductResponse p1 = new ProductResponse(1L, "Keyboard", "Electronics", new BigDecimal("79.99"), 15);
        ProductResponse p2 = new ProductResponse(2L, "Monitor", "Electronics", new BigDecimal("199.99"), 10);

        when(productService.getAllProducts()).thenReturn(Arrays.asList(p1, p2));

        mockMvc.perform(get("/api/products")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Keyboard"))
                .andExpect(jsonPath("$[1].name").value("Monitor"));
    }

    @Test
    @DisplayName("PATCH /api/products/{id}/stock - Should update stock and return 200 OK")
    void shouldUpdateStock() throws Exception {
        StockUpdateRequest request = new StockUpdateRequest(5);
        ProductResponse updated = new ProductResponse(1L, "Keyboard", "Electronics", new BigDecimal("79.99"), 20);

        when(productService.updateStock(eq(1L), eq(5))).thenReturn(updated);

        mockMvc.perform(patch("/api/products/1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(20));
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - Should delete and return 204 No Content")
    void shouldDeleteProduct() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }
}

