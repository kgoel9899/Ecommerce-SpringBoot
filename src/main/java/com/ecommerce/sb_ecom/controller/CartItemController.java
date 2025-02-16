package com.ecommerce.sb_ecom.controller;

import com.ecommerce.sb_ecom.payload.CartItemDTO;
import com.ecommerce.sb_ecom.service.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// just a dummy controller to test deletion by hibernate
@RestController
@RequestMapping("/api")
public class CartItemController {

    @Autowired
    private CartItemService cartItemService;

    @GetMapping("/cart-items")
    public ResponseEntity<List<CartItemDTO>> getCartItems() {
        List<CartItemDTO> cartItemDTOS = cartItemService.getAllCartItems();
        return new ResponseEntity<>(cartItemDTOS, HttpStatus.FOUND);
    }
}
