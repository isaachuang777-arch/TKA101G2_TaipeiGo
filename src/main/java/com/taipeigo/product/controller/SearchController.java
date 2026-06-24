package com.taipeigo.product.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.beans.factory.annotation.Autowired;

import com.taipeigo.product.dto.SearchResultDTO;
import com.taipeigo.product.model.SearchService;
import com.taipeigo.ticketcategory.model.TicketCategoryService;

@Controller
public class SearchController {

    @Autowired
    private SearchService searchService;

    @Autowired
    private TicketCategoryService ticketCategoryService;

    @GetMapping("/search")
    public String searchPage(Model model) {

        model.addAttribute("ticketCategoryList", ticketCategoryService.getAllActive());

        return "frontend/search/searchResult";
    }

    @GetMapping("/api/search")
    @ResponseBody
    public List<SearchResultDTO> getSearchResults(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            // 用來接受前端參數來作價格篩選
            @RequestParam(value = "minPrice", required = false) Integer minPrice,
            @RequestParam(value = "maxPrice", required = false) Integer maxPrice,
            @RequestParam(value = "categoryId", required = false) Integer categoryId

    ) {

        // 將所有參數一起給 Servicr處理
        return searchService.globalSearch(keyword, minPrice, maxPrice, categoryId);

    }

}
