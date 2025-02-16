package com.ecommerce.sb_ecom.service;

import com.ecommerce.sb_ecom.exception.APIException;
import com.ecommerce.sb_ecom.model.CartItem;
import com.ecommerce.sb_ecom.payload.CartItemDTO;
import com.ecommerce.sb_ecom.payload.ProductDTO;
import com.ecommerce.sb_ecom.repository.CartItemRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartItemServiceImpl implements CartItemService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<CartItemDTO> getAllCartItems() {
        List<CartItem> cartItems = cartItemRepository.findAll();
        System.out.println("Kshitij size = " + cartItems.size());

        if (cartItems.isEmpty()) {
            throw new APIException("No cart item exists");
        }

        for(CartItem cartItem : cartItems) {
            System.out.println(cartItem.getProduct());
        }

        List<CartItemDTO> cartItemDTOS = cartItems.stream().map(cartItem -> {
            CartItemDTO cartItemDTO = modelMapper.map(cartItem, CartItemDTO.class);
            cartItemDTO.setCartId(cartItem.getCart().getId());
            ProductDTO productDTO = modelMapper.map(cartItem.getProduct(), ProductDTO.class);
            cartItemDTO.setProduct(productDTO);
            return cartItemDTO;
        }).toList();

        return cartItemDTOS;
    }
}
