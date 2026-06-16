package com.taipeigo.customer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/front/customer")
public class CustomerFrontController {
	
	@GetMapping("/login")
    public String loginPage() {
		return "front-end/customer/login";
	}

    @GetMapping("/register")
    public String registerPage() {
    	return "front-end/customer/register";
    }
}
