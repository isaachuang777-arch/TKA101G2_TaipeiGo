package com.taipeigo.ticket.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.taipeigo.ticket.model.TicketService;
import com.taipeigo.ticket.model.TicketVO;
import com.taipeigo.ticketcategory.model.TicketCategoryService;

@Controller
@RequestMapping("/ticket")
public class TicketFrontendController {
	
	@Autowired
    TicketService ticketService;
    
    /* 進入前台門票首頁 （查全部）*/
    @GetMapping("all")
    public String listAllTicket(ModelMap model) {
        List<TicketVO> list = ticketService.getAll();
        model.addAttribute("ticketListData", list);
        return "frontend/ticket/ticket";
    }
    
    /**
     * 進入門票前台單筆資料：查詢單筆門票詳細資料 
     */
    @GetMapping("detail")
    public String getOneForDisplay(@RequestParam("ticketId") Integer ticketId, ModelMap model) {
        TicketVO ticketVO = ticketService.getOneTicket(ticketId);
        model.addAttribute("ticketVO", ticketVO);
        // 以下是 html 路徑
        return "frontend/ticket/detail"; 
    }

}
