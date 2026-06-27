package com.taipeigo.ticket.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.taipeigo.common.ApiResponse;

import com.taipeigo.ticket.model.TicketDTO;
import com.taipeigo.ticket.model.TicketService;
import com.taipeigo.ticket.model.TicketVO;
import com.taipeigo.ticketcategory.model.TicketCategoryDTO;
import com.taipeigo.ticketcategory.model.TicketCategoryService;
import com.taipeigo.ticketcategory.model.TicketCategoryVO;

@RestController
@RequestMapping("/api/tickets")
public class TicketRestController {

    private final TicketService ticketService;
    private final TicketCategoryService ticketCategoryService;

    @Autowired
    public TicketRestController(TicketService ticketService, TicketCategoryService ticketCategoryService) {
        this.ticketService = ticketService;
        this.ticketCategoryService = ticketCategoryService;
    }

    /**
     * 取得門票清單
     * - 查全部：/api/tickets
     * - 依照分類：/api/tickets?categoryId=3
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllTickets(
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "9") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TicketVO> voPage;

        if (categoryId != null) {
            voPage = ticketService.getActiveTicketsByCategory(categoryId, pageable);
        } else {
            // 只顯示有上架的門票
            voPage = ticketService.getAllEnable(pageable);
        }

        // dto 已處理只會顯示有已啟用的分類
        List<TicketDTO> dtoList = new ArrayList<>();
        for (TicketVO vo : voPage.getContent()) {
            dtoList.add(TicketDTO.fromEntity(vo));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("total", voPage.getTotalElements());
        response.put("totalPages", voPage.getTotalPages());
        response.put("currentPage", voPage.getNumber());
        response.put("data", dtoList);

        return ResponseEntity.ok(ApiResponse.success("查詢成功", response));
    }

    /**
     * 取得單筆門票詳細資料
     * 網址範例：/api/tickets/info?ticketId=1
     */
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<TicketDTO>> getTicketById(@RequestParam("ticketId") Integer ticketId) {
        TicketVO ticketVO = ticketService.getOneTicket(ticketId);
        if (ticketVO == null) {
            return ResponseEntity.status(404).body(ApiResponse.error("找不到該門票商品"));
        }
        return ResponseEntity.ok(ApiResponse.success("查詢成功", TicketDTO.fromEntity(ticketVO)));
    }

    /**
     * 取得所有啟用中的門票分類
     * 網址範例：/api/tickets/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<TicketCategoryDTO>>> getActiveCategories() {
        List<TicketCategoryVO> voList = ticketCategoryService.getAllActive();
        List<TicketCategoryDTO> dtoList = new ArrayList<>();
        for (TicketCategoryVO vo : voList) {
            dtoList.add(new TicketCategoryDTO(vo.getTicketCategoryId(), vo.getTicketCategoryName()));
        }
        return ResponseEntity.ok(ApiResponse.success("查詢成功", dtoList));
    }
}
