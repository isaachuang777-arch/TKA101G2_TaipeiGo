package com.taipeigo.ticket.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

}
