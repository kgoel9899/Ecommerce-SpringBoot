package com.ecommerce.sb_ecom.service;

import com.ecommerce.sb_ecom.payload.CartItemDTO;

import java.util.List;

public interface CartItemService {
    List<CartItemDTO> getAllCartItems();
}
