package com.taipeigo.customer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.taipeigo.customer.model.CustomerService;
import com.taipeigo.customer.model.CustomerVO;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/backend/customer")
public class CustomerController {

	@Value("${taipeigo.upload.base-dir}")
	private String uploadBaseDir;
	
    @Autowired
    private CustomerService customerService;

    // 查全部（列表頁）
    @GetMapping("/list")
    public String listAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            Model model) {
    	
    	List<CustomerVO> allCustomers = customerService.getAllCustomers();

    	model.addAttribute("activeCount",
    	        allCustomers.stream().filter(c -> c.getCustStatus() == 1).count());

    	model.addAttribute("inactiveCount",
    	        allCustomers.stream().filter(c -> c.getCustStatus() == 0).count());

    	model.addAttribute("suspendedCount",
    	        allCustomers.stream().filter(c -> c.getCustStatus() == 2).count());

        Page<CustomerVO> customerPage = customerService.getCustomersByPage(page);

        model.addAttribute("activePage", "customer");
        model.addAttribute("customerList", customerPage.getContent());

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", customerPage.getTotalPages());
        model.addAttribute("totalItems", customerPage.getTotalElements());

        return "backend/customer/listAllCustomer";
    }

    // 去新增頁
    @GetMapping("/add")
    public String addPage(Model model) {
    	
    	model.addAttribute("activePage", "customer");
        model.addAttribute("customerVO", new CustomerVO());
        return "backend/customer/addCustomer";
    }

    // 新增資料
    @PostMapping("/insert")
    public String insert(
            @Valid CustomerVO customerVO,
            BindingResult result,
            Model model) {

        model.addAttribute("activePage", "customer");

        if (result.hasErrors()) {
            return "backend/customer/addCustomer";
        }

        if (customerService.isAccountExist(customerVO.getCustAccount())) {
            model.addAttribute("accountDuplicateError", "此帳號已被使用");
            return "backend/customer/addCustomer";
        }

        if (customerService.isEmailExist(customerVO.getCustEmail())) {
            model.addAttribute("emailDuplicateError", "此 Email 已被註冊");
            return "backend/customer/addCustomer";
        }

        if (customerService.isIdCardExist(customerVO.getCustIdCard())) {
            model.addAttribute("idCardDuplicateError", "此身分證字號已存在");
            return "backend/customer/addCustomer";
        }

        customerService.addCustomer(customerVO);

        return "redirect:/backend/customer/list";
    }
    

    // 去修改頁（先查資料）
    @GetMapping("/edit")
    public String editPage(@RequestParam("id") Integer id, Model model) {

        CustomerVO customerVO = customerService.getOneCustomer(id);
        
        System.out.println(customerVO.getCustBirthday());
        
        model.addAttribute("activePage", "customer");
        model.addAttribute("customerVO", customerVO);

        return "backend/customer/updateCustomer";
    }

    // 修改資料
    @PostMapping("/update")
    public String update(
            @Valid CustomerVO customerVO,
            BindingResult result,
            @RequestParam("imgFile") MultipartFile imgFile,
            Model model) {
    		
    		// 先從資料庫抓原本資料
        CustomerVO db = customerService.getOneCustomer(customerVO.getCustId());
        
        // 修改頁密碼欄位如果沒開放修改，就保留原密碼
        customerVO.setCustPassword(db.getCustPassword());
        
        // 保留原本頭像
        customerVO.setCustImg(db.getCustImg());
        
        // 如果驗證錯誤，回到修改頁，不要 update
        if (result.hasErrors()) {
            model.addAttribute("activePage", "customer");
            model.addAttribute("customerVO", customerVO);
            return "backend/customer/updateCustomer";
        }
        
        try {
            if (imgFile != null && !imgFile.isEmpty()) {

                String originalFileName = imgFile.getOriginalFilename();
                String extension = "";

                if (originalFileName != null && originalFileName.contains(".")) {
                    extension = originalFileName.substring(originalFileName.lastIndexOf("."));
                }

                String fileName = "customer_" + customerVO.getCustId() + extension;

                Path uploadPath = Paths.get(uploadBaseDir, "customer");

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(fileName);

                Files.copy(
                        imgFile.getInputStream(),
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING
                );

                customerVO.setCustImg("customer/" + fileName);
            }

            customerService.updateCustomer(customerVO);

	        } catch (Exception e) {
	            e.printStackTrace();
	            model.addAttribute("activePage", "customer");
	            model.addAttribute("customerVO", customerVO);
	            model.addAttribute("uploadError", "圖片上傳失敗，請重新選擇圖片");
	            return "backend/customer/updateCustomer";
	        }

        		return "redirect:/backend/customer/list";
    }

    // 停權會員（不刪除資料）
    @GetMapping("/suspend")
    public String suspend(@RequestParam("id") Integer id) {

        CustomerVO customerVO = customerService.getOneCustomer(id);

        customerVO.setCustStatus(2); // 2 = 停權

        customerService.updateCustomer(customerVO);

        return "redirect:/backend/customer/list";
    }
    
    // 啟用帳號
    @GetMapping("/activate")
    public String activate(@RequestParam("id") Integer id) {

        CustomerVO customerVO = customerService.getOneCustomer(id);

        customerVO.setCustStatus(1); // 1 = 啟用

        customerService.updateCustomer(customerVO);

        return "redirect:/backend/customer/list";
    }

    // 查單筆（查看詳情）
    @GetMapping("/view")
    public String viewOne(@RequestParam("id") Integer id, Model model) {

        CustomerVO customerVO = customerService.getOneCustomer(id);

        model.addAttribute("activePage", "customer");
        model.addAttribute("customerVO", customerVO);

        return "backend/customer/viewCustomer";
    }
    
    
}