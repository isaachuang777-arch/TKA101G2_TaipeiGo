package com.taipeigo.product.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.taipeigo.product.model.ProductRepository;
import com.taipeigo.product.model.ProductVO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/backend/product")
public class ProductAdminController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/monitor")
    public String showProductMonitor(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            ModelMap model) {
        
        // 1. 取得所有資料計算統計數據
        List<ProductVO> allProducts = productRepository.findAll();
        long totalCount = allProducts.size();
        long ticketCount = allProducts.stream().filter(p -> p.getTicketId() != null).count();
        long activityCount = allProducts.stream().filter(p -> p.getActivityId() != null).count();
        long activeCount = allProducts.stream().filter(p -> p.getStatus() == 1).count();
        long inactiveCount = allProducts.stream().filter(p -> p.getStatus() == 0).count();

        // 2. 取得分頁資料 (每頁 10 筆)
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "productId"));
        Page<ProductVO> pageResult = productRepository.findAll(pageable);

        model.addAttribute("activePage", "productMonitor");
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("productList", pageResult.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResult.getTotalPages());
        
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("ticketCount", ticketCount);
        model.addAttribute("activityCount", activityCount);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("inactiveCount", inactiveCount);

        return "backend/product/monitor";
    }
}
