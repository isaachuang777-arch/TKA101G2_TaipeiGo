package com.taipeigo.faq.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.taipeigo.faq.model.FaqRepository;
import com.taipeigo.faq.model.FaqVO;

@Controller
@RequestMapping("/FAQ")
public class FrontendFaqController {

	@Autowired
	private FaqRepository faqRepository;
	
@GetMapping({"/", "/index",""})
	public String showindexpage(Model model) {
		List<FaqVO> faqVO = faqRepository.findAll();
		model.addAttribute("faqVO", faqVO);
		return "frontend/faq/index";
}
@GetMapping("/faq")
	public String showfaqpage(Model model) {
		List<FaqVO> faqVO = faqRepository.findAll();
		model.addAttribute("faqVO", faqVO);
		return "frontend/faq/faq";
}
@GetMapping("/api")
public String showfaqapipage(Model model) {
	return "frontend/faq/api";
}
}
