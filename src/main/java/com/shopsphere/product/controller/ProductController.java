package com.shopsphere.product.controller;

import com.shopsphere.product.domain.Product;
import com.shopsphere.product.dto.CreateProductRequest;
import com.shopsphere.product.dto.ProductResponse;
import com.shopsphere.product.dto.UpdateProductRequest;
import com.shopsphere.product.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    ) {
        Product product = productService.createProduct(request);
        return ProductResponse.from(product);
    }

    @PatchMapping("/{id}")
    public ProductResponse updateProduct(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        Product updatedProduct = productService.updateProduct(id, request);
        return ProductResponse.from(updatedProduct);
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ProductResponse.from(product);
    }

    @GetMapping("/sku/{sku}")
    public ProductResponse getProductBySku(@PathVariable @NotBlank String sku) {
        Product product = productService.getProductBySku(sku);
        return ProductResponse.from(product);
    }

    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts()
                .stream()
                .map(product -> ProductResponse.from(product))
                .toList();

    }
}
