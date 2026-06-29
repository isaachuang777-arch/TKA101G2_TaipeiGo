package com.taipeigo.faq.controller;

import java.util.ArrayList;
import java.util.List;

import javax.json.Json;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taipeigo.faq.model.FaqRepository;
import com.taipeigo.faq.model.FaqService;
import com.taipeigo.faq.model.FaqVO;
import com.taipeigo.faq.model.FeFaqDto;

@RestController
@RequestMapping("/FAQ/api")
public class FrontendFaqRestController {

	@Autowired
	private FaqRepository faqRepository;
	@Autowired
	private FaqService faqService;
	
	//http://localhost:8080/FAQ/api/all
	@GetMapping("/all")
	public List<FeFaqDto> getallFaq() {
		return faqService.getAllwithouttime();
	}
	//http://localhost:8080/FAQ/api/search?keyword=
	@GetMapping("/search")
	public List<FaqVO> searchFaq(@RequestParam("keyword") String keyword){
		return faqRepository.findByTitleContainingOrContentContaining(keyword,keyword);
	}
	

	
}
