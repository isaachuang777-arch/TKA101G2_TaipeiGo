package com.taipeigo.customer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.taipeigo.customer.model.CustomerVO;

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/customer")
public class FrontendCustomerController {

    @GetMapping("/center")
    public String center(HttpSession session, Model model) {

        CustomerVO loginCustomer = (CustomerVO) session.getAttribute("loginCustomer");

        if (loginCustomer == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("loginCustomer", loginCustomer);

        return "frontend/customer/center";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {

        CustomerVO loginCustomer =
                (CustomerVO) session.getAttribute("loginCustomer");

        if (loginCustomer == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("loginCustomer", loginCustomer);

        return "frontend/customer/profile";
    }

    @GetMapping("/tickets")
    public String tickets(HttpSession session, Model model) {

        CustomerVO loginCustomer =
                (CustomerVO) session.getAttribute("loginCustomer");

        if (loginCustomer == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("loginCustomer", loginCustomer);

        return "frontend/customer/tickets";
    }

    @GetMapping("/password")
    public String password(HttpSession session, Model model) {

        CustomerVO loginCustomer =
                (CustomerVO) session.getAttribute("loginCustomer");

        if (loginCustomer == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("loginCustomer", loginCustomer);

        return "frontend/customer/password";
    }
}