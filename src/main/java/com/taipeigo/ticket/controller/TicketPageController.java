package com.taipeigo.ticket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.beans.factory.annotation.Autowired;
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
    public String showTicketDetail(@RequestParam(value = "ticketId", required = false) Integer ticketId) {
        if (ticketId != null) {
            TicketVO ticketVO = ticketService.getOneTicket(ticketId);
            // 檢查結果，如果找不到該門票或商品已下架(status != 1)，導回門票列表頁
            if (ticketVO == null || ticketVO.getTicketStatus() != 1) {
                return "redirect:/ticket";
            }
        } else {
            return "redirect:/ticket";
        }
        return "frontend/ticket/ticketDetail";
    }
}
