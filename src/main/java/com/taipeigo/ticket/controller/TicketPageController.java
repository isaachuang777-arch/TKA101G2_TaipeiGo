package com.taipeigo.ticket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.taipeigo.ticket.model.TicketService;
import com.taipeigo.ticket.model.TicketVO;

/**
 * 前台門票頁面渲染控制器 (回傳 HTML 網頁路徑)
 */
@Controller
@RequestMapping("/ticket")
public class TicketPageController {

    @Autowired
    private TicketService ticketService;

    /**
     * 進入前台門票首頁
     */
    @GetMapping()
    public String listAllTicket() {
        return "frontend/ticket/ticket";
    }

    /**
     * 進入門票前台單筆詳情頁面
     */
    @GetMapping("detail")
    public String getOneForDisplay(@RequestParam("ticketId") Integer ticketId, ModelMap model) {
        TicketVO ticketVO = ticketService.getOneTicket(ticketId);
        model.addAttribute("ticket", ticketVO);
        return "frontend/ticket/ticketDetail";
    }
}
