package com.taipeigo.product.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

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
    public ResponseEntity<Map<String, Object>> getSearchResults(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "minPrice", required = false) Integer minPrice,
            @RequestParam(value = "maxPrice", required = false) Integer maxPrice,
            @RequestParam(value = "categoryIds", required = false) List<Integer> categoryIds,
            @RequestParam(value = "type", required = false, defaultValue = "ALL") String type,
            @RequestParam(value = "sortBy", required = false, defaultValue = "relevance") String sortBy,

            // 分頁參數
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size
    ) {
        Map<String, Object> response = searchService.globalSearch(keyword, minPrice, maxPrice, categoryIds, type, page, size, sortBy);
        return ResponseEntity.ok(response);
    }

}
