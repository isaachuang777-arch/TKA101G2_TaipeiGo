package com.taipeigo.customer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.taipeigo.customer.model.CustomerVO;

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/frontend/customer")
public class FrontendCustomerController {

    @GetMapping("/center")
    public String center(HttpSession session, Model model) {

        CustomerVO loginCustomer = (CustomerVO) session.getAttribute("loginCustomer");
        
	    // 目前先在 Controller 驗證是否登入
	    // 避免使用者直接輸入網址進入會員中心
	    // 後續若改由 FrontendLoginFilter 統一驗證，可移除此段
        if (loginCustomer == null) {
            return "redirect:/frontend/auth/login";
        }
        
        // 將登入會員資料傳給 HTML
        model.addAttribute("loginCustomer", loginCustomer);

        return "frontend/customer/center";
    }
    
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {

        CustomerVO loginCustomer =
                (CustomerVO) session.getAttribute("loginCustomer");

        if (loginCustomer == null) {
            return "redirect:/frontend/auth/login";
        }

        model.addAttribute("loginCustomer", loginCustomer);

        return "frontend/customer/profile";
    }
    
    @GetMapping("/tickets")
    public String tickets(HttpSession session, Model model) {

        CustomerVO loginCustomer =
                (CustomerVO) session.getAttribute("loginCustomer");

        if (loginCustomer == null) {
            return "redirect:/frontend/auth/login";
        }

        model.addAttribute("loginCustomer", loginCustomer);

        return "frontend/customer/tickets";
    }
    
}