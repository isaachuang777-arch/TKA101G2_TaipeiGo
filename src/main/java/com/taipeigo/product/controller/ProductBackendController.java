package com.taipeigo.product.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.taipeigo.activity.model.ActivityRepository;
import com.taipeigo.product.model.ProductService;
import com.taipeigo.product.model.ProductVO;
import com.taipeigo.ticket.model.TicketRepository;
import org.springframework.data.domain.Page;

import jakarta.validation.Valid;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;




@Controller
@RequestMapping("/backend/product")
public class ProductBackendController {

    private final ProductService productService;
    private final ActivityRepository activityRepository;
    private final TicketRepository ticketRepository;

    @Autowired
    public ProductBackendController(ProductService productService, ActivityRepository activityRepository, TicketRepository ticketRepository){

        this.productService = productService;
        this.activityRepository = activityRepository;
        this.ticketRepository = ticketRepository;
    }

    // 商品列表

     @GetMapping({"", "/"})
     public String listProducts(
        @RequestParam(value = "keyword", required = false) String keyword, 
        @RequestParam(value = "page", defaultValue = "1") int page,
        Model model){

            Page<ProductVO> productPage = productService.getProductsByKeyword(keyword, page);

            model.addAttribute("productList", productPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPage", productPage.getTotalPages() == 0 ? 1 : productPage.getTotalPages());
            model.addAttribute("keyword", keyword);
            
            // 將所有活動與票券傳至前端，供 Inline 編輯下拉選單使用
            model.addAttribute("activityList", activityRepository.findAll());
            model.addAttribute("ticketList", ticketRepository.findAll());
            model.addAttribute("activePage", "product");

            return "backend/product/list";

        }
    
    // 新增頁面

    @GetMapping("/add")
    public String addProductPage(Model model){
        
        model.addAttribute("product", new ProductVO()); 

        model.addAttribute("activityList", activityRepository.findAll());
        
        model.addAttribute("ticketList", ticketRepository.findAll());

         return "backend/product/add";
    }

    // 新增頁面送出

    @PostMapping("/add")
    public String addProductSubmit(@Valid @ModelAttribute("product") ProductVO product, BindingResult result, Model model){

        // 防呆 避免出錯再把資料塞回去
        if(result.hasErrors()){
            
            model.addAttribute("activityList", activityRepository.findAll());

            model.addAttribute("ticketList", ticketRepository.findAll());

            return "backend/product/add"; 
        }
        productService.addProduct(product);

        return "redirect:/backend/product";
    }

    // 修改頁面 (已移除，改用 Inline 編輯)
    
    // API: Inline 編輯商品 (更新商品名稱或綁定 ID)
    @PostMapping("/api/update/{id}")
    @ResponseBody
    public ResponseEntity<?> updateProductInline(@PathVariable("id") Integer id, @RequestBody ProductVO payload) {
        ProductVO product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 更新允許修改的欄位
        if (payload.getProductName() != null) {
            product.setProductName(payload.getProductName());
        }
        
        // 這裡允許 activityId / ticketId 設為 null (代表解除綁定)
        product.setActivityId(payload.getActivityId());
        product.setTicketId(payload.getTicketId());
        
        ProductVO updatedProduct = productService.updateProduct(product);
        return ResponseEntity.ok(updatedProduct);
    }
    
    // 6. 切換上下架狀態 (給 AJAX 呼叫的)
    @PostMapping("/api/toggle/{id}")
    @ResponseBody
    public ResponseEntity<?> toggleProductStatusAPI(@PathVariable("id") Integer id) {
        ProductVO updatedProduct = productService.toggleProductStatus(id);
        if (updatedProduct == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedProduct);
    }

}
