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

@Controller
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    // =========================
    // 1️⃣ 查全部（列表頁）
    // =========================
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

        return "back-end/customer/listAllCustomer";
    }

    // =========================
    // 2️⃣ 去新增頁
    // =========================
    @GetMapping("/add")
    public String addPage(Model model) {
    	
    	model.addAttribute("activePage", "customer");
        model.addAttribute("customerVO", new CustomerVO());
        return "back-end/customer/addCustomer";
    }

    // =========================
    // 3️⃣ 新增資料
    // =========================
    @PostMapping("/insert")
    public String insert(
            @Valid CustomerVO customerVO,
            BindingResult result,
            Model model) {

        if(result.hasErrors()) {
        	model.addAttribute("customerVO", customerVO);
            return "back-end/customer/addCustomer";
        }

        customerService.addCustomer(customerVO);

        return "redirect:/customer/list";
    }

    // =========================
    // 4️⃣ 去修改頁（先查資料）
    // =========================
    @GetMapping("/edit")
    public String editPage(@RequestParam("id") Integer id, Model model) {

        CustomerVO customerVO = customerService.getOneCustomer(id);
        
        System.out.println(customerVO.getCustBirthday());
        
        model.addAttribute("activePage", "customer");
        model.addAttribute("customerVO", customerVO);

        return "back-end/customer/updateCustomer";
    }

    // =========================
    // 5️⃣ 修改資料
    // =========================
    @PostMapping("/update")
    public String update(CustomerVO customerVO) {

        CustomerVO db = customerService.getOneCustomer(customerVO.getCustId());
        customerVO.setCustPassword(db.getCustPassword());

        customerService.updateCustomer(customerVO);
        return "redirect:/customer/list";
    }

    // =========================
    // 6️⃣ 刪除資料
    // =========================
    @GetMapping("/delete")
    public String delete(@RequestParam("id") Integer id) {

        customerService.deleteCustomer(id);
        return "redirect:/customer/list";
    }

    // =========================
    // 7️⃣ 查單筆（查看詳情）
    // =========================
    @GetMapping("/view")
    public String viewOne(@RequestParam("id") Integer id, Model model) {

        CustomerVO customerVO = customerService.getOneCustomer(id);

        model.addAttribute("activePage", "customer");
        model.addAttribute("customerVO", customerVO);

        return "back-end/customer/viewCustomer";
    }
    
    
}