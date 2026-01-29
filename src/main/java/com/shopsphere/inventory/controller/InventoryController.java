package com.shopsphere.inventory.controller;

import com.shopsphere.inventory.domain.InventoryReservation;
import com.shopsphere.inventory.dto.ReserveStockRequest;
import com.shopsphere.inventory.service.InventoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/reserve")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryReservation reserveStock(
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @Valid @RequestBody ReserveStockRequest request
    ){
        return inventoryService.reserveStock(
                request.getProductId(),
                request.getOrderId(),
                request.getQuantity(),
                idempotencyKey
        );
    }

    @PostMapping("/confirm/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    public void confirm(@PathVariable Long orderId){
        inventoryService.confirmReservation(orderId);
    }

    @PostMapping("/release/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    public void release(@PathVariable Long orderId){
        inventoryService.releaseReservation(orderId);
    }
}
