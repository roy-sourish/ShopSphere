package com.shopsphere.product.controller;

import com.shopsphere.product.domain.Product;
import com.shopsphere.product.dto.CreateProductRequest;
import com.shopsphere.product.dto.ProductResponse;
import com.shopsphere.product.dto.UpdateProductRequest;
import com.shopsphere.product.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@Validated
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(
            @Valid @RequestBody CreateProductRequest request
    ){
        Product product = productService.createProduct(request);
        return ProductResponse.from(product);
    }

    @PatchMapping("/{id}")
    public ProductResponse updateProduct(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody UpdateProductRequest request
    ){
        Product updatedProduct = productService.updateProduct(id, request);
        return ProductResponse.from(updatedProduct);
    }
}
