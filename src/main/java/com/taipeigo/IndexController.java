package com.taipeigo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.taipeigo.ticket.model.TicketService;
import com.taipeigo.ticket.model.TicketVO;

@Controller
@RequestMapping("/")
public class IndexController {

	@Autowired
	private TicketService ticketService;

	@GetMapping
	public String home(ModelMap model) {
		// public String home(HttpSession session) {
		// CustomerVO customer = new CustomerVO();
		//
		// customer.setCustName("王小明");
		//
		// session.setAttribute("loginCustomer", customer);
		//
		// return "frontend/index/index";

		// 熱門景點門票
		List<TicketVO> popularTickets = ticketService.getPopularTickets(3);
		model.addAttribute("popularTickets", popularTickets);
		return "frontend/index/index";
	}
	@GetMapping("/aboutus")
	public String showaboutme() {
		return  "frontend/aboutus/aboutus";
	}
}
