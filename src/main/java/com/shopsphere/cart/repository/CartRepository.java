package com.shopsphere.cart.repository;

import com.shopsphere.cart.domain.Cart;
import com.shopsphere.cart.domain.CartStatus;
import com.shopsphere.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserIdAndStatus(Long userId, CartStatus status);

    Long user(User user);
}
