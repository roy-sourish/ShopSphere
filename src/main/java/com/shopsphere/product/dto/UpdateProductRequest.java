package com.shopsphere.product.dto;

import jakarta.validation.constraints.PositiveOrZero;

public class UpdateProductRequest {
    private String name;

    @PositiveOrZero(message = "Stock cannot be negative")
    private Integer stockQuantity;

    public UpdateProductRequest(){}

    public String getName(){
        return name;
    }

    public Integer getStockQuantity(){
        return stockQuantity;
    }
}
