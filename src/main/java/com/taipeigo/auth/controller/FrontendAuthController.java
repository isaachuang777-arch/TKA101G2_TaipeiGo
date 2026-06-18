package com.taipeigo.auth.controller;

import com.taipeigo.customer.model.CustomerService;
import com.taipeigo.customer.model.CustomerVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/frontend")
public class FrontendAuthController {

    @Autowired
    private CustomerService customerService;

    // 顯示前台會員登入頁
    // 網址：GET /frontend/auth/login
    // 對應：templates/frontend/auth/login.html
    @GetMapping("/auth/login")
    public String showLoginPage() {
        return "frontend/auth/login";
    }

    // 處理前台會員登入
    // 網址：POST /frontend/auth/login
    @PostMapping("/auth/login")
    public String login(@RequestParam("custAccount") String custAccount,
                        @RequestParam("custPassword") String custPassword,
                        HttpSession session,
                        Model model) {

        // 依照帳號去資料庫查會員
        CustomerVO customer = customerService.findByAccount(custAccount);

        // 查不到會員，代表帳號不存在
        if (customer == null) {
            model.addAttribute("errorMsg", "帳號不存在");
            return "frontend/auth/login";
        }

        // 密碼不一致，代表密碼錯誤
        if (!customer.getCustPassword().equals(custPassword)) {
            model.addAttribute("errorMsg", "密碼錯誤");
            return "frontend/auth/login";
        }

        // 0 = 未啟用，不允許登入
        if (customer.getCustStatus() != null && customer.getCustStatus() == 0) {
            model.addAttribute("errorMsg", "帳號尚未啟用");
            return "frontend/auth/login";
        }

        // 2 = 停權，不允許登入
        if (customer.getCustStatus() != null && customer.getCustStatus() == 2) {
            model.addAttribute("errorMsg", "帳號已停權，請聯絡客服");
            return "frontend/auth/login";
        }

        // 登入成功，把會員資料存進 Session
        // 之後訂單、票券、收藏都可以用 loginCustomer 取得目前登入會員
        session.setAttribute("loginCustomer", customer);

        // 登入成功後回前台首頁
        return "redirect:/";
    }

    // 前台會員登出
    // 網址：GET /frontend/auth/logout
    @GetMapping("/auth/logout")
    public String logout(HttpSession session) {

        // 移除登入狀態
        session.removeAttribute("loginCustomer");

        // 登出後回前台首頁
        return "redirect:/";
    }
}