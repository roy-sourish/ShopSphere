package com.shopsphere.product.service;

import com.shopsphere.common.exception.OptimisticConflictException;
import com.shopsphere.product.domain.Product;
import com.shopsphere.product.dto.CreateProductRequest;
import com.shopsphere.product.dto.UpdateProductRequest;
import com.shopsphere.product.exception.DuplicateProductException;
import com.shopsphere.product.exception.ProductNotFoundException;
import com.shopsphere.product.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // Step 3: Force flush for constraint + version conflict detection
        try{
            productRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateProductException(product.getSku());
        } catch (ObjectOptimisticLockingFailureException ex){
            throw new OptimisticConflictException("Product", productId);
        }
        return product;
    }

    @Transactional
    public void reduceStock(Long productId, int quantity){
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.decreaseStock(quantity);

        // Force optimistic locking check immediately
        try {
            productRepository.flush();
        }catch (ObjectOptimisticLockingFailureException ex){
            throw new OptimisticConflictException("Product", productId);
        }
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id){
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Product getProductBySku(String sku){
        return productRepository.findBySku(sku.trim().toUpperCase())
                .orElseThrow(() -> new ProductNotFoundException(sku));
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts(){
        return productRepository.findAll();
    }
}
