package com.taipeigo.ticketcategory.controller;


import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.taipeigo.ticketcategory.model.TicketCategoryService;
import com.taipeigo.ticketcategory.model.TicketCategoryVO;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/ticketCategory")
public class TicketCategoryController {

    @Autowired
    TicketCategoryService ticketCategoryService;
    
    
    /* 進入票券種類列表 (查全部) */
    @GetMapping("listAllCategory")
    public String listAllCategory(ModelMap model) {
        List<TicketCategoryVO> list = ticketCategoryService.getAll();
        model.addAttribute("ticketCategoryListData", list); 
        return "backend/ticketCategory/listAllTicketCategory";    
    }
    
    /* 進入新增門票頁 */
    @GetMapping("addTicketCategory")
    public String addTicketCategory(ModelMap model) {
        TicketCategoryVO ticketCategoryVO = new TicketCategoryVO();
        model.addAttribute("ticketCategoryVO", ticketCategoryVO); 
        return "backend/ticketCategory/addTicketCategory";
    }
    
    /* 處理新增票券種類表單送出 */
    @PostMapping("insert")
    public String insert(@Valid TicketCategoryVO ticketCategoryVO, 
                         BindingResult result, ModelMap model) {
        if (result.hasErrors()) {
        	// 以下是 html 檔案路徑
            return "backend/ticketCategory/addTicketCategory";
        }
        ticketCategoryService.addTicketCategory(ticketCategoryVO); 
        // 以下是 網頁網址路徑
        return "redirect:/ticketCategory/listAllCategory";
    }


}