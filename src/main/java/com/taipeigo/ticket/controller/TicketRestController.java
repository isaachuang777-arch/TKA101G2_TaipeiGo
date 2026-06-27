package com.taipeigo.ticket.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<List<TicketDTO>> getAllTickets(
            @RequestParam(value = "categoryId", required = false) Integer categoryId) {
        List<TicketVO> voList;

        if (categoryId != null) {
            voList = ticketService.getActiveTicketsByCategory(categoryId);
        } else {
            // 只顯示有上架的門票
            voList = ticketService.getAllEnable();
        }

        // dto 已處理只會顯示有已啟用的分類
        List<TicketDTO> dtoList = new ArrayList<>();
        for (TicketVO vo : voList) {
            dtoList.add(TicketDTO.fromEntity(vo));
        }
        return ResponseEntity.ok(dtoList);
    }

    /**
     * 取得單筆門票詳細資料
     * 網址範例：/api/tickets/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<TicketDTO> getTicketById(@PathVariable("id") Integer id) {
        TicketVO ticketVO = ticketService.getOneTicket(id);
        if (ticketVO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(TicketDTO.fromEntity(ticketVO));
    }

    /**
     * 取得所有啟用中的門票分類
     * 網址範例：/api/tickets/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<TicketCategoryDTO>> getActiveCategories() {
        List<TicketCategoryVO> voList = ticketCategoryService.getAllActive();
        List<TicketCategoryDTO> dtoList = new ArrayList<>();
        for (TicketCategoryVO vo : voList) {
            dtoList.add(new TicketCategoryDTO(vo.getTicketCategoryId(), vo.getTicketCategoryName()));
        }
        return ResponseEntity.ok(dtoList);
    }
}
