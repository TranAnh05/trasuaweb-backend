package com.example.trasuaweb_backend.repositories;

import com.example.trasuaweb_backend.entities.OrderItemTopping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemToppingRepository extends JpaRepository<OrderItemTopping, Long> {
}
