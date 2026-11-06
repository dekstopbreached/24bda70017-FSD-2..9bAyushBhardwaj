package com.example.cipipeline.service;

import com.example.cipipeline.dto.ProductRequest;
import com.example.cipipeline.dto.ProductResponse;
import com.example.cipipeline.exception.InvalidStockException;
import com.example.cipipeline.exception.ProductNotFoundException;
import com.example.cipipeline.model.Product;
import com.example.cipipeline.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product(
                request.getName(),
                request.getCategory(),
                request.getPrice(),
                request.getStock()
        );
        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse updateStock(Long id, Integer quantityChange) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        int newStock = product.getStock() + quantityChange;
        if (newStock < 0) {
            throw new InvalidStockException("Insufficient stock. Current: " + product.getStock() + ", Requested change: " + quantityChange);
        }

        product.setStock(newStock);
        Product updated = productRepository.save(product);
        return mapToResponse(updated);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }

    public ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getStock()
        );
    }
}

