package com.shopsphere.inventory;

import com.shopsphere.cart.domain.Cart;
import com.shopsphere.cart.repository.CartRepository;
import com.shopsphere.inventory.domain.InventoryReservation;
import com.shopsphere.inventory.domain.ReservationStatus;
import com.shopsphere.inventory.exception.ReservationExpiredException;
import com.shopsphere.inventory.repository.InventoryReservationRepository;
import com.shopsphere.inventory.service.InventoryReservationService;
import com.shopsphere.order.domain.Order;
import com.shopsphere.order.repository.OrderRepository;
import com.shopsphere.product.domain.Product;
import com.shopsphere.product.repository.ProductRepository;
import com.shopsphere.user.domain.User;
import com.shopsphere.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class InventoryReservationServiceTest {

    @Autowired
    private InventoryReservationService reservationService;

    @Autowired
    private InventoryReservationRepository reservationRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    private User createUser() {
        return userRepository.save(new User("inventory@test.com", "password"));
    }

    private Product createProduct(String sku, int stock) {
        return productRepository.save(
                new Product(sku, "Inventory Test Product", BigDecimal.valueOf(10_000), stock)
        );
    }

    private Cart createCartWithItem(User user, Product product, int quantity) {
        Cart cart = new Cart(user);
        cart.addItem(product, quantity);
        return cartRepository.save(cart);
    }

    @Test
    void reserveCartItems_decrementsStock_andCreatesReservations() {
        User user = createUser();
        Product product = createProduct("SKU-RESERVE-1", 10);
        Cart cart = createCartWithItem(user, product, 3);

        List<InventoryReservation> reservations = reservationService.reserveCartItems(cart);

        Product reloaded = productRepository.findById(product.getId()).orElseThrow();
        assertThat(reloaded.getStockQuantity()).isEqualTo(7);

        assertThat(reservations).hasSize(1);
        InventoryReservation reservation = reservations.get(0);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.ACTIVE);
        assertThat(reservation.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void attachOrderToReservations_whenExpired_releasesStock_andMarksExpired() {
        User user = createUser();
        Product product = createProduct("SKU-RESERVE-2", 5);
        Cart cart = createCartWithItem(user, product, 2);

        product.decreaseStock(2);
        productRepository.saveAndFlush(product);

        InventoryReservation reservation = new InventoryReservation(
                user.getId(),
                cart.getId(),
                product.getId(),
                2,
                Instant.now().minusSeconds(60)
        );
        reservationRepository.saveAndFlush(reservation);

        assertThatThrownBy(() -> reservationService.attachOrderToReservations(cart.getId(), 1L))
                .isInstanceOf(ReservationExpiredException.class);

        Product reloaded = productRepository.findById(product.getId()).orElseThrow();
        InventoryReservation updated = reservationRepository.findById(reservation.getId()).orElseThrow();

        assertThat(reloaded.getStockQuantity()).isEqualTo(5);
        assertThat(updated.getStatus()).isEqualTo(ReservationStatus.EXPIRED);
    }

    @Test
    void consumeReservations_whenExpired_releasesStock_andMarksExpired() {
        User user = createUser();
        Product product = createProduct("SKU-RESERVE-3", 4);
        Cart cart = createCartWithItem(user, product, 1);

        Order order = orderRepository.saveAndFlush(new Order(user, "USD"));

        product.decreaseStock(1);
        productRepository.saveAndFlush(product);

        InventoryReservation reservation = new InventoryReservation(
                user.getId(),
                cart.getId(),
                product.getId(),
                1,
                Instant.now().minusSeconds(30)
        );
        reservation.attachOrder(order.getId());
        reservationRepository.saveAndFlush(reservation);

        assertThatThrownBy(() -> reservationService.consumeReservations(order.getId()))
                .isInstanceOf(ReservationExpiredException.class);

        Product reloaded = productRepository.findById(product.getId()).orElseThrow();
        InventoryReservation updated = reservationRepository.findById(reservation.getId()).orElseThrow();

        assertThat(reloaded.getStockQuantity()).isEqualTo(4);
        assertThat(updated.getStatus()).isEqualTo(ReservationStatus.EXPIRED);
    }

    @Test
    void releaseExpiredReservations_restoresStock_andMarksExpired() {
        User user = createUser();
        Product product = createProduct("SKU-RESERVE-4", 6);
        Cart cart = createCartWithItem(user, product, 2);

        product.decreaseStock(2);
        productRepository.saveAndFlush(product);

        InventoryReservation reservation = new InventoryReservation(
                user.getId(),
                cart.getId(),
                product.getId(),
                2,
                Instant.now().minusSeconds(120)
        );
        reservationRepository.saveAndFlush(reservation);

        reservationService.releaseExpiredReservations();

        Product reloaded = productRepository.findById(product.getId()).orElseThrow();
        InventoryReservation updated = reservationRepository.findById(reservation.getId()).orElseThrow();

        assertThat(reloaded.getStockQuantity()).isEqualTo(6);
        assertThat(updated.getStatus()).isEqualTo(ReservationStatus.EXPIRED);
    }
}
