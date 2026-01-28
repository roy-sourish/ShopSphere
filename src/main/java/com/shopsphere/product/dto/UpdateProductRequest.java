package com.shopsphere.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public class UpdateProductRequest {
    @NotBlank(message = "Product name cannot be blank")
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
