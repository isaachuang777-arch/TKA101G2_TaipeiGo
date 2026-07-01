package com.taipeigo.myticket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.taipeigo.myticket.model.MyTicketService;
import com.taipeigo.ticket.model.TicketSerialVO;

@Controller
public class MyTicketVerifyController {

    @Autowired
    private MyTicketService myTicketService;

    // 掃描 QR Code 後，先進確認頁，不直接驗票
    @GetMapping("/ticket/verify/{serialNumber}")
    public String verifyConfirm(
            @PathVariable String serialNumber,
            Model model) {

        TicketSerialVO ticket =
                myTicketService.getTicketBySerialNumber(serialNumber);

        String message =
                myTicketService.checkTicketBeforeVerify(ticket);

        model.addAttribute("ticket", ticket);
        model.addAttribute("message", message);

        // 如果票券不可驗，直接進結果頁
        if (!"OK".equals(message)) {
            return "frontend/myticket/verifyResult";
        }

        // 如果票券可驗，進確認頁
        return "frontend/myticket/verifyConfirm";
    }

    // 驗票員按下「確認驗票」後，才真正改成已使用
    @PostMapping("/ticket/verify/{serialNumber}/confirm")
    public String verifySubmit(
            @PathVariable String serialNumber,
            Model model) {

        TicketSerialVO ticket =
                myTicketService.getTicketBySerialNumber(serialNumber);

        String message =
                myTicketService.confirmVerifyTicket(serialNumber);

        model.addAttribute("ticket", ticket);
        model.addAttribute("message", message);

        return "frontend/myticket/verifyResult";
    }
}