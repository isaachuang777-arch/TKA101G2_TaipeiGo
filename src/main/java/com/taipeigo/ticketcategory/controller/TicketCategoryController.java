package com.taipeigo.ticketcategory.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.*;
import com.taipeigo.ticketcategory.model.TicketCategoryVO;
import com.taipeigo.ticketcategory.model.TicketCategoryService;

@Controller
@RequestMapping("/ticketCategory")
public class TicketCategoryController {

    @Autowired
    TicketCategoryService ticketCategoryService;
    
    
    /* 票券種類列表 (查全部) */
    @GetMapping("listAllCategory")
    public String listAllCategory(ModelMap model) {
        List<TicketCategoryVO> list = ticketCategoryService.getAll();
        model.addAttribute("ticketCategoryListData", list); 
        return "backend/ticketCategory/listAllTicketCategory";    
    }


}