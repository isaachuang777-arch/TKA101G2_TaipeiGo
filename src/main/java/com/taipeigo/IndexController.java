package com.taipeigo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.taipeigo.customer.model.CustomerVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/")
public class IndexController {
	@GetMapping
	public String home() {
//		public String home(HttpSession session) {
//		    CustomerVO customer = new CustomerVO();
//
//		    customer.setCustName("王小明");
//
//		    session.setAttribute("loginCustomer", customer);
//
//		    return "frontend/index/index";

		return "frontend/index/index";
//	    }

	}
}
