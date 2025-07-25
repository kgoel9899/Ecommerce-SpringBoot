package com.ecommerce.sb_ecom.controller;

import com.ecommerce.sb_ecom.payload.OrderDTO;
import com.ecommerce.sb_ecom.payload.OrderRequestDTO;
import com.ecommerce.sb_ecom.service.OrderService;
import com.ecommerce.sb_ecom.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthUtil authUtil;

    @PostMapping("/order/users/payments/{paymentMethod}")
    public ResponseEntity<OrderDTO> orderProducts(@PathVariable String paymentMethod,
                                                  @RequestBody OrderRequestDTO orderRequestDTO) {
        String emailId = authUtil.loggedInEmail();
        OrderDTO order = orderService.placeOrder(emailId, orderRequestDTO.getAddressId(), paymentMethod);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }
}
