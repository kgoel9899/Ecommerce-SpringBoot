package com.ecommerce.sb_ecom.repository;

import com.ecommerce.sb_ecom.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
