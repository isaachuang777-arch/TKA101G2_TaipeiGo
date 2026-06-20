package com.taipeigo.ticket.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.taipeigo.ticket.model.TicketService;
import com.taipeigo.ticket.model.TicketVO;


@Controller
@RequestMapping("/ticket")
public class TicketController {
	
	@Autowired
	TicketService ticketService;
	
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
    @GetMapping("/getOne_For_Display")
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

}
