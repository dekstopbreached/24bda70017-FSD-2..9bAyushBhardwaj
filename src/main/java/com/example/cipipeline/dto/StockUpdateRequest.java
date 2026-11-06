package com.example.cipipeline.dto;

import jakarta.validation.constraints.NotNull;

public class StockUpdateRequest {

    @NotNull(message = "Quantity change cannot be null")
    private Integer quantityChange;

    public StockUpdateRequest() {
    }

    public StockUpdateRequest(Integer quantityChange) {
        this.quantityChange = quantityChange;
    }

    public Integer getQuantityChange() {
        return quantityChange;
    }

    public void setQuantityChange(Integer quantityChange) {
        this.quantityChange = quantityChange;
    }
}
