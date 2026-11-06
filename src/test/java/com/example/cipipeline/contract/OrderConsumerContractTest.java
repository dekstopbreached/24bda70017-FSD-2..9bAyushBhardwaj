package com.example.cipipeline.contract;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "ProductServiceProvider", pactVersion = PactSpecVersion.V3)
@DisplayName("Contract Testing (Consumer): OrderService -> ProductService Pact")
class OrderConsumerContractTest {

    @Pact(consumer = "OrderServiceConsumer")
    public RequestResponsePact createProductPact(PactDslWithProvider builder) {
        return builder
                .given("Product with ID 1 exists")
                .uponReceiving("A GET request for product details with ID 1")
                .path("/api/products/1")
                .method("GET")
                .matchHeader("Accept", ".*application/json.*", "application/json")
                .willRespondWith()
                .status(200)
                .headers(Map.of("Content-Type", "application/json"))
                .body(new PactDslJsonBody()
                        .integerType("id", 1L)
                        .stringValue("name", "Gaming Laptop")
                        .stringValue("category", "Computers")
                        .decimalType("price", 1299.99)
                        .integerType("stock", 10))
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createProductPact")
    @DisplayName("Consumer verifies contract by querying the Pact mock server")
    void testOrderConsumerReceivesProduct(MockServer mockServer) {
        RestTemplate restTemplate = new RestTemplate();
        String url = mockServer.getUrl() + "/api/products/1";

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().get("id"));
        assertEquals("Gaming Laptop", response.getBody().get("name"));
        assertEquals("Computers", response.getBody().get("category"));
        assertEquals(1299.99, ((Number) response.getBody().get("price")).doubleValue(), 0.01);
        assertEquals(10, response.getBody().get("stock"));
    }
}
