package com.shopsphere.product.service;

import com.shopsphere.product.domain.Product;
import com.shopsphere.product.dto.CreateProductRequest;
import com.shopsphere.product.dto.UpdateProductRequest;
import com.shopsphere.product.exception.DuplicateProductException;
import com.shopsphere.product.exception.ProductNotFoundException;
import com.shopsphere.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product createProduct(CreateProductRequest request) {
        Product product = new Product(
                request.getSku(),
                request.getName(),
                request.getPrice(),
                request.getStockQuantity()
        );

        try {
            return productRepository.save(product);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateProductException(request.getSku());
        }
    }

    @Transactional
    public Product updateProduct(Long productId, UpdateProductRequest request) {
        // Step 1: Load product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // Step 2: Apply patch
        if(request.getName() != null){
            product.changeName(request.getName());
        }

        if(request.getStockQuantity() != null){
            product.changeStock(request.getStockQuantity());
        }

        // Step 3: No save() needed
        // Hibernate dirty checking will flush changes automatically

        return product;
    }

    @Transactional
    public void reduceStock(Long productId, int quantity){
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.decreaseStock(quantity);
    }

    @Transactional
    public Product getProductById(Long id){
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional
    public Product getProductBySku(String sku){
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException(sku));
    }

    @Transactional
    public List<Product> getAllProducts(){
        return productRepository.findAll();
    }
}
