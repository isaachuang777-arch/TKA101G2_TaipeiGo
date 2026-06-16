package com.taipeigo.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.taipeigo.customer.model.CustomerService;

@Controller
@RequestMapping("/backend")
public class BackendController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        model.addAttribute("activePage", "dashboard");

        model.addAttribute("customerList",
                            customerService.getAllCustomers());

        return "backend/dashboard/index";
    }
}

