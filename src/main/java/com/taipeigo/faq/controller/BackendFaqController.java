package com.taipeigo.faq.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.taipeigo.faq.model.FaqService;
import com.taipeigo.faq.model.FaqVO;

import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@RequestMapping("/backend/faq")
public class BackendFaqController {

    @Autowired
    private FaqService faqService;

    @GetMapping({"/", "/index", "/list"})
    public String listAllFaq(
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<FaqVO> faqPage = faqService.getFaqByPage(page);

        model.addAttribute("faqList", faqPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", faqPage.getTotalPages());
        model.addAttribute("totalItems", faqPage.getTotalElements());
        model.addAttribute("activePage", "faq");

        return "backend/faq/list";
    }
    
    @GetMapping("/add")
    public String addFaqPage(Model model) {
        model.addAttribute("faqVO", new FaqVO());
        model.addAttribute("activePage", "faq");
        return "backend/faq/add";
    }
    
    @PostMapping("/insert")
    public String insertFaq(
            @Valid @ModelAttribute("faqVO") FaqVO faqVO,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("activePage", "faq");
            return "backend/faq/add";
        }

        faqService.addFaq(faqVO);

        return "redirect:/backend/faq/list";
    }
    
    @GetMapping("/edit")
    public String editFaqPage(@RequestParam("faqId") Integer faqId, Model model) {
        FaqVO faqVO = faqService.getOneFaq(faqId);

        model.addAttribute("faqVO", faqVO);
        model.addAttribute("activePage", "faq");

        return "backend/faq/edit";
    }
    
    @PostMapping("/update")
    public String updateFaq(
            @Valid @ModelAttribute("faqVO") FaqVO faqVO,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("activePage", "faq");
            return "backend/faq/edit";
        }

        faqService.updateFaq(faqVO);

        return "redirect:/backend/faq/list";
    }
    
    @GetMapping("/delete")
    public String deleteFaq(@RequestParam("faqId") Integer faqId) {
        faqService.deleteFaq(faqId);
        return "redirect:/backend/faq/list";
    }
}