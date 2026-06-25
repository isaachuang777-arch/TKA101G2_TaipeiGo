package com.taipeigo.myticket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.taipeigo.myticket.model.MyTicketService;
import com.taipeigo.ticket.model.TicketSerialVO;

@Controller
public class MyTicketVerifyController {

    @Autowired
    private MyTicketService myTicketService;

    @GetMapping("/ticket/verify/{serialNumber}")
    public String verifyTicket(
            @PathVariable String serialNumber,
            Model model) {

        // 先查票券
        TicketSerialVO ticket =
                myTicketService.getTicketBySerialNumber(serialNumber);

        // 驗票
        String message =
                myTicketService.verifyAndUseTicket(serialNumber);

        model.addAttribute("message", message);

        // 有查到票券才放進 Model
        if (ticket != null) {
            model.addAttribute("ticket", ticket);
        }

        return "frontend/myticket/verifyResult";
    }
}