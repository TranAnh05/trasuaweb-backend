package com.example.trasuaweb_backend.repositories;

import com.example.trasuaweb_backend.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
