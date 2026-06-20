package com.taipeigo.ticket.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.taipeigo.ticket.model.TicketService;
import com.taipeigo.ticket.model.TicketVO;
import com.taipeigo.ticketcategory.model.TicketCategoryService;


@Controller
@RequestMapping("/ticket")
public class TicketController {
	
	@Autowired
	TicketService ticketService;

    @Autowired
    private TicketCategoryService ticketCategoryService;
	
	/* 進入門票頁面 （查全部）*/
	@GetMapping("listAllTicket")
	public String listAllTicket(ModelMap model) {
		List<TicketVO> list = ticketService.getAll();
		model.addAttribute("ticketListData", list);
		return "backend/ticket/listAllTicket";
	}
	
	/**
     * 查詢單筆門票詳細資料 
     * 網址範例：/backend/ticket/getOne_For_Display?ticketId=1
     */
    @GetMapping("getOne_For_Display")
    public String getOneForDisplay(@RequestParam("ticketId") Integer ticketId, ModelMap model) {
        TicketVO ticketVO = ticketService.getOneTicket(ticketId);
        model.addAttribute("ticketVO", ticketVO);
        // 以下是 html 路徑
        return "backend/ticket/listOneTicket"; 
    }
    
    /**
     * RESTful API 查詢單筆門票 (回傳 JSON 格式)
     */
    /* TODO: 後續 Thymeleaf 改前端
    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketVO> getTicketJson(@PathVariable Integer ticketId) {
        TicketVO ticketVO = ticketService.getOneTicket(ticketId);
        if (ticketVO == null) {
            return ResponseEntity.notFound().build(); 
        }
        return ResponseEntity.ok(ticketVO);
    }
    */

    @GetMapping("/addTicket")
    public String addTicket(ModelMap model) {
        TicketVO ticketVO = new TicketVO();
        model.addAttribute("ticketVO", ticketVO);
        // 查出所有啟用中的門票分類， th:each="category : ${categoryList}"
        model.addAttribute("categoryList", ticketCategoryService.getAllActive()); 
        // 以下是 html 路徑
        return "backend/ticket/addTicket"; 
    }

    
    @PostMapping("/generateSerials") // 對應 th:action 網址
    public String generateSerials(
            @RequestParam("ticketId") Integer ticketId,   
            @RequestParam("quantity") int quantity,  
            RedirectAttributes redirectAttributes) {    
        
        try {
            ticketService.generateSerials(ticketId, quantity);
            // 新增成功訊息
            redirectAttributes.addFlashAttribute("success", "成功為門票商品 (編號:" + ticketId + ") 新增了 " + quantity + " 張序號！");
            
        } catch (Exception e) {
            // 錯誤訊息
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "新增序號失敗：" + e.getMessage());
        }
        // 以下是 網頁網址路徑
        return "redirect:/ticket/listAllTicket"; 
    }

}
