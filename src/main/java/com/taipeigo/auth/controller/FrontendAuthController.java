package com.taipeigo.auth.controller;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.taipeigo.cart.model.CartService;
import com.taipeigo.customer.model.CustomerService;
import com.taipeigo.customer.model.CustomerVO;

import jakarta.servlet.http.HttpSession;

import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class FrontendAuthController {

    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${spring.mail.password}")
    private String mailPassword;
    
    // 顯示前台會員登入頁
    // 網址：GET /auth/login
    // 對應：templates/frontend/auth/login.html
    @GetMapping("/login")
    public String showLoginPage(
        // 用來接收前端傳來導回網址(AJAX用的,js傳過來)
        @RequestParam(value = "redirect", required = false) String redirect,
        HttpSession session) {

        if (redirect != null && !redirect.isEmpty()) {
            session.setAttribute("frontendReUrl", redirect);
        }  
        
        return "frontend/auth/login";
    }

    // 處理前台會員登入
    // 網址：POST /auth/login
    @PostMapping("/login")
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
        session.setAttribute("loginCustomer", customer);
        
     // 同步未登入購物車到 Redis
        cartService.mergeTempCart(session);

        // 如果原本是被 Filter 擋下來的頁面，登入後導回原頁
        String frontendReUrl = (String) session.getAttribute("frontendReUrl");

        if (frontendReUrl != null) {
            session.removeAttribute("frontendReUrl");
            return "redirect:" + frontendReUrl;
        }

        // 沒有原頁就回前台首頁
        return "redirect:/";
    }
    
	// 顯示註冊頁面
	// 使用 GET 請求進入註冊頁
	// 並提供 CustomerVO 給表單綁定
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
    	
        model.addAttribute("customerVO", new CustomerVO());

        return "frontend/auth/register";
    }
    
	// 接收會員註冊資料
	// 註冊後預設為未啟用帳號，並產生 Email 驗證 token 存入 Redis
	@PostMapping("/register")
	public String register(
	        @Valid CustomerVO customerVO,
	        BindingResult result,
	        Model model,
	        RedirectAttributes redirectAttributes) {
		
		System.out.println("custStatus = " + customerVO.getCustStatus());
		
		// 先檢查 VO 驗證
		if(result.hasErrors()) {
	        return "frontend/auth/register";
	    }
		
		// 帳號重複檢查
		if (customerService.isAccountExist(customerVO.getCustAccount())) {
			model.addAttribute("errorMsg", "此帳號已被使用");
			return "frontend/auth/register";
		}

		// Email 重複檢查
		if (customerService.isEmailExist(customerVO.getCustEmail())) {
			model.addAttribute("errorMsg", "此 Email 已被註冊");
			return "frontend/auth/register";
		}

		// 身分證重複檢查
		if (customerService.isIdCardExist(customerVO.getCustIdCard())) {
			model.addAttribute("errorMsg", "身分證字號無法使用，請確認資料或聯絡客服");
			return "frontend/auth/register";
		}

		customerVO.setCustStatus(0); // 0 = 未啟用

		customerService.addCustomer(customerVO);

		// 產生驗證 token
		String token = UUID.randomUUID().toString();

		// 存入 Redis：verify:token -> custId，有效 30 分鐘
		stringRedisTemplate.opsForValue().set("verify:" + token, customerVO.getCustId().toString(), 30,
				TimeUnit.MINUTES);

		String verifyUrl = "http://localhost:8080/auth/verify?token=" + token;

		sendVerifyEmail(customerVO.getCustEmail(), verifyUrl);

		System.out.println("驗證信已寄出：" + customerVO.getCustEmail());

		redirectAttributes.addFlashAttribute(
		        "successMsg",
		        "驗證信已寄出！請前往您的電子郵件收信，完成帳號驗證後即可登入。"
		);
		
		return "redirect:/auth/login";
	}
	
	// Email 驗證
	// 網址：GET /auth/verify?token=xxxx
	@GetMapping("/verify")
	public String verifyEmail(
	        @RequestParam("token") String token,
	        RedirectAttributes redirectAttributes) {

	    String custId = stringRedisTemplate.opsForValue().get("verify:" + token);

	    if (custId == null) {
	    		redirectAttributes.addFlashAttribute("errorMsg", "驗證連結已失效，請重新註冊或重新寄送驗證信");
	    		return "redirect:/auth/login";
	    }

	    CustomerVO customer = customerService.getOneCustomer(Integer.valueOf(custId));

	    if (customer == null) {
	    		redirectAttributes.addFlashAttribute("errorMsg", "查無會員資料");
	    		return "redirect:/auth/login";
	    }

	    customer.setCustStatus(1); // 1 = 啟用
	    customerService.updateCustomer(customer);

	    stringRedisTemplate.delete("verify:" + token);

	    redirectAttributes.addFlashAttribute(
	            "successMsg",
	            "Email 驗證成功，現在可以登入！"
	    );

	    return "redirect:/auth/login";
	}
	

    // 前台會員登出
    // 網址：GET /auth/logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {

        // 移除登入狀態
        session.removeAttribute("loginCustomer");

        // 登出後回前台首頁
        return "redirect:/";
    }
    
    
    private void sendVerifyEmail(String toEmail, String verifyUrl) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailUsername, mailPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailUsername, "TaipeiGo 驗證中心"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("TaipeiGo 會員驗證信");

            message.setText(
                    "您好，歡迎註冊 TaipeiGo！\n\n"
                  + "請點擊以下連結完成帳號驗證：\n"
                  + verifyUrl
                  + "\n\n此連結 30 分鐘內有效。"
            );

            Transport.send(message);

            System.out.println("Gmail 寄送成功");

        } catch (Exception e) {

            System.out.println("Gmail 寄送失敗");
            e.printStackTrace();

        }
    }
    
    // 忘記密碼頁面
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "frontend/auth/forgot-password";
    }
    
    // 寄送忘記密碼驗證信
    @PostMapping("/forgot-password")
    public String forgotPassword(
            @RequestParam("email") String email,
            Model model) {

        CustomerVO customerVO = customerService.findByEmail(email);

        if (customerVO == null) {
        	
            model.addAttribute(
                    "errorMessage",
                    "查無此電子信箱。");
            
            return "frontend/auth/forgot-password";
        }
        
        String token = UUID.randomUUID().toString();

        stringRedisTemplate.opsForValue().set(
                "resetpwd:" + token,
                customerVO.getCustId().toString(),
                30,
                TimeUnit.MINUTES
        );

        String resetUrl = "http://localhost:8080/auth/reset-password?token=" + token;
        
        sendResetPasswordEmail(customerVO.getCustEmail(), resetUrl);
        
        model.addAttribute(
                "successMessage",
                "重設密碼信已寄出，請前往您的電子郵件收信。"
        );

        return "frontend/auth/forgot-password";
    }
    
    private void sendResetPasswordEmail(String toEmail, String resetUrl) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailUsername, mailPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailUsername, "TaipeiGo 會員中心"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("TaipeiGo 重設密碼通知");

            message.setText(
                    "我們收到您的密碼重設申請。\n\n"
                  + "請點擊以下連結重新設定密碼：\n"
                  + resetUrl
                  + "\n\n此連結 30 分鐘內有效。"
                  + "\n\n如果您沒有申請重設密碼，請忽略此信。"
            );

            Transport.send(message);

            System.out.println("重設密碼信寄送成功");

        } catch (Exception e) {

            System.out.println("重設密碼信寄送失敗");
            e.printStackTrace();

        }
    }
    
    // 顯示重設密碼頁面
    @GetMapping("/reset-password")
    public String showResetPasswordPage(
            @RequestParam("token") String token,
            Model model) {

        String custId = stringRedisTemplate.opsForValue().get("resetpwd:" + token);

        if (custId == null) {
            model.addAttribute("errorMessage", "重設密碼連結已失效，請重新申請。");
            return "frontend/auth/forgot-password";
        }

        model.addAttribute("token", token);

        return "frontend/auth/reset-password";
    }
    
    
    // 處理重設密碼
    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam("token") String token,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes) {

        String custId = stringRedisTemplate.opsForValue().get("resetpwd:" + token);

        if (custId == null) {
            model.addAttribute("errorMessage", "重設密碼連結已失效，請重新申請。");
            return "frontend/auth/forgot-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "兩次輸入的密碼不一致。");
            model.addAttribute("token", token);
            return "frontend/auth/reset-password";
        }
        
        if (!newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,20}$")) {
            model.addAttribute(
                    "errorMessage",
                    "密碼需 8~20 字元，且必須包含大寫、小寫英文字母及數字，不可包含特殊字元"
            );
            model.addAttribute("token", token);
            return "frontend/auth/reset-password";
        }

        CustomerVO customerVO = customerService.getOneCustomer(Integer.valueOf(custId));
        customerVO.setCustPassword(newPassword);
        customerService.updateCustomer(customerVO);

        stringRedisTemplate.delete("resetpwd:" + token);

        redirectAttributes.addFlashAttribute("successMsg", "密碼重設成功，請使用新密碼登入。");

        return "redirect:/auth/login";
    }
    
}