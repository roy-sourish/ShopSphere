package com.shopsphere.inventory;

import com.shopsphere.inventory.domain.InventoryReservation;
import com.shopsphere.inventory.domain.InventoryStatus;
import com.shopsphere.inventory.exception.InsufficientStockException;
import com.shopsphere.inventory.exception.InvalidReservationStateException;
import com.shopsphere.inventory.repository.InventoryReservationRepository;
import com.shopsphere.inventory.service.InventoryCleanupService;
import com.shopsphere.inventory.service.InventoryService;
import com.shopsphere.product.domain.Product;
import com.shopsphere.product.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class InventoryServiceTest {
    @Autowired
    InventoryService inventoryService;
    @Autowired
    InventoryCleanupService cleanupService;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    InventoryReservationRepository reservationRepository;

    @Autowired
    EntityManager entityManager;

    /**
     * TEST 1 — Reserve Reduces Stock <br>
     * Reserving stock actually decrements product inventory.
     *
     */
    @Test
    void reserveStock_shouldReduceStock() {
        // Arrange
        Product product = new Product(
                "SKU-1",
                "Test Product",
                new BigDecimal("100.00"),
                10
        );

        productRepository.saveAndFlush(product);

        // Act: reserve 3 units
        inventoryService.reserveStock(
                product.getId(),
                500L,
                3,
                "idem-1"
        );

        // Assert: stock becomes 7
        Product updated = productRepository.findById(product.getId()).get();

        assertThat(updated.getStockQuantity()).isEqualTo(7);
    }

    /**
     * TEST 2 — Retry Does NOT Double-Decrement Stock <br>
     * Same idempotency key must not reduce stock twice.
     *
     */
    @Test
    void reserveStock_retryWithSameKey_shouldNotDoubleDecrement() {

        Product product = new Product(
                "SKU-2",
                "Retry Product",
                new BigDecimal("50.00"),
                10
        );

        productRepository.saveAndFlush(product);

        // First request
        inventoryService.reserveStock(product.getId(), 501L, 3, "idem-retry");

        // Retry request (same key)
        inventoryService.reserveStock(product.getId(), 501L, 3, "idem-retry");

        // Stock should only reduce once
        Product updated =
                productRepository.findById(product.getId()).get();

        assertThat(updated.getStockQuantity()).isEqualTo(7);
    }

    /**
     * TEST 3 — Insufficient Stock Throws Exception <br>
     * You cannot reserve more than available.
     *
     */
    @Test
    void reserveStock_shouldFailIfInsufficientStock() {
        Product product = new Product(
                "SKU-3",
                "Low Stock Product",
                new BigDecimal("20.00"),
                2
        );

        productRepository.saveAndFlush(product);

        // Act + Assert
        assertThatThrownBy(() ->
                inventoryService.reserveStock(
                        product.getId(),
                        502L,
                        5,
                        "idem-fail"
                )
        ).isInstanceOf(InsufficientStockException.class);
    }

    /**
     * TEST 4 — Release Restores Stock <br>
     * Cancelling an order restores stock.
     *
     */
    @Test
    void releaseReservation_shouldRestoreStock() {
        Product product = new Product(
                "SKU-4",
                "Release Product",
                new BigDecimal("30.00"),
                10
        );

        productRepository.saveAndFlush(product);

        inventoryService.reserveStock(
                product.getId(),
                503L,
                4,
                "idem-rel"
        );

        // Release order
        inventoryService.releaseReservation(503L);

        Product updated =
                productRepository.findById(product.getId()).get();

        assertThat(updated.getStockQuantity()).isEqualTo(10);
    }

    /**
     * Test 5: Confirm Marks Reservation PURCHASED <br>
     * Payment success finalizes reservation.
     *
     */
    @Test
    void confirmReservation_shouldMarkPurchased() {

        Product product = new Product(
                "SKU-5",
                "Confirm Product",
                new BigDecimal("80.00"),
                10
        );

        productRepository.saveAndFlush(product);

        InventoryReservation reservation =
                inventoryService.reserveStock(product.getId(), 504L, 2, "idem-conf");

        // Confirm
        inventoryService.confirmReservation(504L);

        InventoryReservation updated =
                reservationRepository.findById(reservation.getId()).get();

        assertThat(updated.getStatus()).isEqualTo(InventoryStatus.PURCHASED);
    }

    /**
     * TEST 6 — Cleanup Expires Old Reservations <br>
     * Expired holds are automatically released.
     */
    @Test
    void cleanup_shouldReleaseExpiredReservations() {

        Product product = new Product(
                "SKU-6",
                "Expiry Product",
                new BigDecimal("60.00"),
                5
        );

        productRepository.saveAndFlush(product);

        inventoryService.reserveStock(product.getId(), 505L, 2, "idem-exp");

        // Make reservation artificially old
        InventoryReservation reservation =
                reservationRepository.findAllByOrderId(505L).get(0);

        // Force DB timestamp update
        entityManager.createQuery("""
                            update InventoryReservation r
                            set r.createdAt = :time
                            where r.id = :id
                        """)
                .setParameter("time", Instant.now().minus(20, ChronoUnit.MINUTES))
                .setParameter("id", reservation.getId())
                .executeUpdate();

        // Clear persistence context so we fetch fresh DB values
        entityManager.clear();


        reservationRepository.saveAndFlush(reservation);

        // Run cleanup
        cleanupService.expireOldReservations();

        // Stock restored
        Product updated =
                productRepository.findById(product.getId()).get();

        assertThat(updated.getStockQuantity()).isEqualTo(5);

        // Reservation released
        InventoryReservation updatedRes =
                reservationRepository.findById(reservation.getId()).get();

        assertThat(updatedRes.getStatus()).isEqualTo(InventoryStatus.RELEASED);
    }

    /**
     * TEST 7 — Illegal Transition: Confirm After Release Should Fail <br>
     * Once a reservation is RELEASED, it cannot be confirmed. <br>
     * Real-world meaning:
     * <ul>
     *     <li>stock was returned</li>
     *     <li>someone else might have bought it</li>
     *     <li>payment callback arrived too late</li>
     * </ul>
     * So confirm must return conflict.
     */
    @Test
    void confirmAfterRelease_shouldFail() {

        // Arrange
        Product product = new Product(
                "SKU-7",
                "Illegal Transition Product",
                new BigDecimal("100.00"),
                5
        );
        productRepository.saveAndFlush(product);

        // Reserve Stock
        inventoryService.reserveStock(product.getId(), 600L, 2, "idem-illegal");

        // Act: Release first
        inventoryService.releaseReservation(600L);

        // Assert: Confirm after release should fail
        assertThatThrownBy(() ->
                inventoryService.confirmReservation(600L)
        ).isInstanceOf(InvalidReservationStateException.class);
    }

    /**
     * TEST 8 — Multi-Item Order Confirm Works for All Items <br>
     * One order can reserve multiple products. <br>
     * Confirm must mark ALL as PURCHASED.
     *
     */
    @Test
    void confirmReservation_shouldPurchaseAllItemsInOrder() {

        // Arrange: two products
        Product p1 = new Product("SKU-8A", "Product A",
                new BigDecimal("50.00"), 10);

        Product p2 = new Product("SKU-8B", "Product B",
                new BigDecimal("30.00"), 10);

        productRepository.saveAndFlush(p1);
        productRepository.saveAndFlush(p2);

        // Reserve both under same order
        inventoryService.reserveStock(p1.getId(), 700L, 2, "idem-multi-1");
        inventoryService.reserveStock(p2.getId(), 700L, 1, "idem-multi-2");

        // Act: Confirm order
        inventoryService.confirmReservation(700L);

        // Assert: Fetch all reservations which are PURCHASED
        List<InventoryReservation> reservations =
                reservationRepository.findAllByOrderId(700L);

        // Size check
        assertEquals(2, reservations.size());

        // Status check
        for (InventoryReservation r : reservations) {
            assertEquals(InventoryStatus.PURCHASED, r.getStatus());
        }
    }

    /**
     * TEST 9 — Idempotency Key Payload Mismatch Should Fail <br>
     * Client must not reuse the same idempotency key for a different request. <br>
     * Example: <br>
     * First reserve qty=2 with key=abc <br>
     * Then reserve qty=5 with same key=abc <br>
     * That is invalid.
     *
     */
    @Test
    void idempotencyKeyReuseWithDifferentPayload_shouldFail() {

        Product product = new Product(
                "SKU-9",
                "Mismatch Product",
                new BigDecimal("40.00"),
                10
        );

        productRepository.saveAndFlush(product);

        // First reservation
        inventoryService.reserveStock(product.getId(), 800L, 2, "idem-mismatch");

        // Retry with same key but different quantity
        assertThatThrownBy(() ->
                inventoryService.reserveStock(product.getId(), 800L, 5, "idem-mismatch")
        ).isInstanceOf(IllegalArgumentException.class);
    }

}
