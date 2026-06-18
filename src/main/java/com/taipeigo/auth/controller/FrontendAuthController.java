package com.taipeigo.auth.controller;

import com.taipeigo.customer.model.CustomerService;
import com.taipeigo.customer.model.CustomerVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/frontend")
public class FrontendAuthController {

    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
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
    
	// 顯示註冊頁面
	// 使用 GET 請求進入註冊頁
	// 並提供 CustomerVO 給表單綁定
    @GetMapping("/auth/register")
    public String showRegisterPage(Model model) {

        model.addAttribute("customerVO", new CustomerVO());

        return "frontend/auth/register";
    }
    
	// 接收會員註冊資料
	// 註冊後預設為未啟用帳號，並產生 Email 驗證 token 存入 Redis
	@PostMapping("/auth/register")
	public String register(CustomerVO customerVO, Model model) {

    customerVO.setCustStatus(0); // 0 = 未啟用

    customerService.addCustomer(customerVO);

    // 產生驗證 token
    String token = UUID.randomUUID().toString();

    // 存入 Redis：verify:token -> custId，有效 30 分鐘
    stringRedisTemplate.opsForValue().set(
             "verify:" + token,
             customerVO.getCustId().toString(),
             30,
             TimeUnit.MINUTES
    );

    // 先用 Console 模擬 Email 驗證連結
    System.out.println("驗證連結：http://localhost:8080/frontend/auth/verify?token=" + token);

    model.addAttribute("successMsg", "註冊成功，請至信箱完成驗證");
    return "frontend/auth/login";
	}
	
	// Email 驗證
	// 網址：GET /frontend/auth/verify?token=xxxx
	@GetMapping("/auth/verify")
	public String verifyEmail(@RequestParam("token") String token, Model model) {

	    String custId = stringRedisTemplate.opsForValue().get("verify:" + token);

	    if (custId == null) {
	        model.addAttribute("errorMsg", "驗證連結已失效，請重新註冊或重新寄送驗證信");
	        return "frontend/auth/login";
	    }

	    CustomerVO customer = customerService.getOneCustomer(Integer.valueOf(custId));

	    if (customer == null) {
	        model.addAttribute("errorMsg", "查無會員資料");
	        return "frontend/auth/login";
	    }

	    customer.setCustStatus(1); // 1 = 啟用
	    customerService.updateCustomer(customer);

	    stringRedisTemplate.delete("verify:" + token);

	    model.addAttribute("successMsg", "Email 驗證成功，請重新登入");
	    return "frontend/auth/login";
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