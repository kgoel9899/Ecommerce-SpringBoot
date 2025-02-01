package com.ecommerce.sb_ecom.repository;

import com.ecommerce.sb_ecom.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
