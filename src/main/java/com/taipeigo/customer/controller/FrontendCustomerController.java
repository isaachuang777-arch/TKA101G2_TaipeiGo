package com.taipeigo.customer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.taipeigo.customer.model.CustomerVO;

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/customer")
public class FrontendCustomerController {

    @ModelAttribute
    public void addLoginCustomer(HttpSession session, Model model) {
        CustomerVO loginCustomer =
                (CustomerVO) session.getAttribute("loginCustomer");

        model.addAttribute("loginCustomer", loginCustomer);
    }

    @GetMapping("/center")
    public String center() {
        return "frontend/customer/center";
    }

    @GetMapping("/profile")
    public String profile() {
        return "frontend/customer/profile";
    }

    @GetMapping("/tickets")
    public String tickets() {
        return "frontend/customer/tickets";
    }

    @GetMapping("/password")
    public String password() {
        return "frontend/customer/password";
    }
}