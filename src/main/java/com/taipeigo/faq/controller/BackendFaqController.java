package com.taipeigo.faq.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.taipeigo.faq.model.FaqRepository;

@Controller
@RequestMapping("/backend/faq")
public class BackendFaqController {
	@Autowired
	private FaqRepository faqRepository;
	
	@GetMapping({"/", "/index"})
	public String showindexpage() {
		return "backend/faq/index";
}
	

}
