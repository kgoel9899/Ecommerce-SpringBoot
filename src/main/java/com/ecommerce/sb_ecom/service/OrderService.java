package com.ecommerce.sb_ecom.service;

import com.ecommerce.sb_ecom.payload.OrderDTO;
import jakarta.transaction.Transactional;

public interface OrderService {
    @Transactional
    OrderDTO placeOrder(String emailId, Long addressId, String paymentMethod);
}
