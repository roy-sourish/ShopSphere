package com.shopsphere.product.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long productId) {
        super("Product not found with id: " + productId);
    }

    public ProductNotFoundException(String sku) {
        super("Product with sku " + sku + " not found");
    }
}
