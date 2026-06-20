package com.taipeigo.cs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.taipeigo.cs.model.CsService;
import com.taipeigo.cs.model.CsVO;
import com.taipeigo.customer.model.CustomerVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/CustomerService")

public class FrontendcsController {
	
	@Autowired
	private CsService csService;
	
//掛客服中心首頁
	@GetMapping({"/index","/"})
	public String showCsIndexPage() {
		return "frontend/cs/index";
	}

//掛新增詢問頁面
	@GetMapping("/createticket")
	public String showcreateTicketpage() {
		return "frontend/cs/createticket";
	}
	
//按下新增詢問  CsVO createTicket(Integer custId, Byte caseCate, String msg, String msgImgsrc)
	@PostMapping("/createticket")
	public String createticketToDB(HttpSession session, 
			@RequestParam("caseCate") Byte caseCate,
			@RequestParam("msg") String msg, 
			@RequestParam (value = "msgImgsrc", required = false) String msgImgsrc, 
			Model model,
			RedirectAttributes redirectAttributes) {
		//1. who create the ticket
		CustomerVO customerVO = (CustomerVO) session.getAttribute("loginCustomer");
		Integer custId= customerVO.getCustId();
		//2. 上傳圖片
			//TODO
		//3.
		try {
			 csService.createTicket(custId, caseCate, msg, msgImgsrc);
			 redirectAttributes.addFlashAttribute("successMsg", "謝謝您的詢問，已經新增一個問題，請耐心等待回覆。");
		}catch(RuntimeException e) {
			redirectAttributes.addFlashAttribute("errorMsg",  e.getMessage());
		}
			
		return ("redirect:/CustomerService/index");
	}
}
