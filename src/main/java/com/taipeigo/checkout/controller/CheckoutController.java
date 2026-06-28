package com.taipeigo.checkout.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.taipeigo.checkout.model.CheckoutService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/frontend/checkout")

public class CheckoutController {
    @Autowired
    private CheckoutService checkoutService;

    @PostMapping
    public String checkout(HttpSession session) {

        checkoutService.checkout(session);

        return "redirect:/frontend/customer/orders";
    }
}
