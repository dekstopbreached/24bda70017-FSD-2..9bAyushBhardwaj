package com.example.cipipeline.contract;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import com.example.cipipeline.controller.ProductController;
import com.example.cipipeline.dto.ProductResponse;
import com.example.cipipeline.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;

@Provider("ProductServiceProvider")
@PactFolder("target/pacts")
@DisplayName("Contract Testing (Provider): Verify ProductService satisfies OrderService Pact")
class ProductProviderVerificationTest {

    private ProductService productService;

    @BeforeEach
    void before(PactVerificationContext context) {
        productService = Mockito.mock(ProductService.class);
        ProductController productController = new ProductController(productService);

        MockMvcTestTarget testTarget = new MockMvcTestTarget();
        testTarget.setControllers(productController);
        if (context != null) {
            context.setTarget(testTarget);
        }
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
        }
    }

    @State("Product with ID 1 exists")
    void toProductExistsState() {
        ProductResponse response = new ProductResponse(
                1L,
                "Gaming Laptop",
                "Computers",
                new BigDecimal("1299.99"),
                10
        );
        when(productService.getProductById(1L)).thenReturn(response);
    }
}
