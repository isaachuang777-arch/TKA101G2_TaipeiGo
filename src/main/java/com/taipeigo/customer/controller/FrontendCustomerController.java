package com.taipeigo.customer.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.taipeigo.customer.model.CustomerVO;
import com.taipeigo.myticket.model.MyTicketService;
import com.taipeigo.ticket.model.TicketSerialVO;

import jakarta.servlet.http.HttpSession;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.taipeigo.customer.model.CustomerService;
import java.time.LocalDate;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/customer")
public class FrontendCustomerController {

    @Autowired
    private MyTicketService myTicketService;
    
    @Autowired
    private CustomerService customerService;

    @Value("${taipeigo.upload.base-dir}")
    private String uploadBaseDir;

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
    public String tickets(
            HttpSession session,
            Model model,
            HttpServletRequest request) {

        CustomerVO loginCustomer =
                (CustomerVO) session.getAttribute("loginCustomer");

        System.out.println("目前登入會員ID = " + loginCustomer.getCustId());

        List<TicketSerialVO> myTickets =
                myTicketService.getMyTickets(loginCustomer.getCustId());

        System.out.println("查到票券數量 = " + myTickets.size());
        
        String baseUrl =
                request.getScheme()
                + "://"
                + request.getServerName()
                + ":"
                + request.getServerPort();

        model.addAttribute("baseUrl", baseUrl);
        model.addAttribute("myTickets", myTickets);
        model.addAttribute("activePage", "tickets");

        return "frontend/customer/tickets";
    }

    @GetMapping("/password")
    public String password() {
        return "frontend/customer/password";
    }
    
    @PostMapping("/updatePassword")
    public String updatePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session,
            Model model) {

        CustomerVO loginCustomer =
                (CustomerVO) session.getAttribute("loginCustomer");

        if (loginCustomer == null) {
            return "redirect:/auth/login";
        }

        CustomerVO db =
                customerService.getOneCustomer(loginCustomer.getCustId());

        if (!db.getCustPassword().equals(oldPassword)) {
            model.addAttribute("errorMessage", "舊密碼錯誤");
            return "frontend/customer/password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "新密碼與確認新密碼不一致");
            return "frontend/customer/password";
        }

        if (!newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z0-9]{8,20}$")) {
            model.addAttribute("errorMessage", "新密碼需 8~20 字元，且必須包含大寫、小寫英文字母及數字，不可包含特殊字元");
            return "frontend/customer/password";
        }

        db.setCustPassword(newPassword);
        customerService.updateCustomer(db);

        session.setAttribute("loginCustomer", db);

        model.addAttribute("successMessage", "密碼修改成功");

        return "frontend/customer/password";
    }
    
    @PostMapping("/updateAvatar")
    public String updateAvatar(
            @RequestParam("avatarFile") MultipartFile avatarFile,
            HttpSession session) {

        CustomerVO loginCustomer =
                (CustomerVO) session.getAttribute("loginCustomer");

        if (loginCustomer == null) {
            return "redirect:/auth/login";
        }

        if (avatarFile == null || avatarFile.isEmpty()) {
            return "redirect:/customer/profile";
        }

        try {
            CustomerVO db =
                    customerService.getOneCustomer(loginCustomer.getCustId());

            String originalFileName = avatarFile.getOriginalFilename();
            String extension = "";

            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName
                        .substring(originalFileName.lastIndexOf("."))
                        .toLowerCase();
            }

            // 簡單限制圖片格式
            if (!extension.equals(".jpg")
                    && !extension.equals(".jpeg")
                    && !extension.equals(".png")
                    && !extension.equals(".webp")) {
                return "redirect:/customer/profile";
            }

            String fileName = "customer_" + db.getCustId() + extension;

            Path uploadPath = Paths.get(uploadBaseDir, "customer");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);

            Files.copy(
                    avatarFile.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            db.setCustImg("customer/" + fileName);

            customerService.updateCustomer(db);

            session.setAttribute("loginCustomer", db);

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/customer/profile";
        }

        return "redirect:/customer/profile";
    }
    
    @PostMapping("/updateProfile")
    public String updateProfile(
            @RequestParam String custName,
            @RequestParam String custTel,
            @RequestParam String custSex,
            @RequestParam String custBirthday,
            @RequestParam(required = false) String custAddress,
            HttpSession session,
            Model model) {

        CustomerVO loginCustomer =
                (CustomerVO) session.getAttribute("loginCustomer");

        if (loginCustomer == null) {
            return "redirect:/auth/login";
        }
        
        if (custName == null || custName.trim().isEmpty()) {
            model.addAttribute("errorMessage", "姓名不可空白");
            return "frontend/customer/profile";
        }

        if (custTel == null || !custTel.matches("^09[0-9]{8}$")) {
            model.addAttribute("errorMessage", "手機號碼需為 09 開頭的 10 碼數字");
            return "frontend/customer/profile";
        }

        if (custSex == null || !custSex.matches("^[mMfF]$")) {
            model.addAttribute("errorMessage", "性別資料格式錯誤");
            return "frontend/customer/profile";
        }

        try {

            // 從資料庫抓最新資料
            CustomerVO db =
                    customerService.getOneCustomer(loginCustomer.getCustId());

            // 更新允許修改欄位
            db.setCustName(custName);
            db.setCustTel(custTel);
            db.setCustSex(custSex);
            db.setCustAddress(custAddress);

            if (custBirthday != null && !custBirthday.isBlank()) {
            		db.setCustBirthday(LocalDate.parse(custBirthday));
            }

            customerService.updateCustomer(db);

            // 更新 Session
            session.setAttribute("loginCustomer", db);
            
            // 讓本次畫面也使用更新後的會員資料
            model.addAttribute("loginCustomer", db);
            
            model.addAttribute("successMessage", "個人資料更新成功");

        } catch (Exception e) {

            e.printStackTrace();

            model.addAttribute(
                    "errorMessage",
                    "個人資料更新失敗");
        }

        return "frontend/customer/profile";
    }
}