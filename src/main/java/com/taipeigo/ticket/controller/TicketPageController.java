package com.taipeigo.ticket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 前台門票頁面渲染控制器 (回傳 HTML 網頁路徑)
 */
@Controller
@RequestMapping("/ticket")
public class TicketPageController {

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
    public String showTicketDetail() {
        return "frontend/ticket/ticketDetail";
    }
}
