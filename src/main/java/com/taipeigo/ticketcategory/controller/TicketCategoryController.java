package com.taipeigo.ticketcategory.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.taipeigo.ticketcategory.model.TicketCategoryService;
import com.taipeigo.ticketcategory.model.TicketCategoryVO;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/backend/ticketCategory")
public class TicketCategoryController {

    @Autowired
    TicketCategoryService ticketCategoryService;

    /* 進入票券種類列表頁面 (查全部未被刪除) (分頁 + 搜尋) */
    @GetMapping("list")
    public String listAllCategory(
            ModelMap model,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "keyword", required = false) String keyword) {

        Page<TicketCategoryVO> pageResult;

        if (keyword != null && !keyword.trim().isEmpty()) {
            model.addAttribute("keyword", keyword.trim());
            pageResult = ticketCategoryService.searchByName(keyword.trim(), page);
        } else {
            pageResult = ticketCategoryService.getNotDeletedWithPage(page);
        }

        // 計算 總分類數、啟用數、未啟用數
        List<TicketCategoryVO> allNotDeletedList = ticketCategoryService.getAllNotDeleted();
        long totalCount = allNotDeletedList.size();
        long activeCount = allNotDeletedList.stream().filter(vo -> vo.getTicketCategoryStatus() == 1).count();
        long inactiveCount = allNotDeletedList.stream().filter(vo -> vo.getTicketCategoryStatus() == 0).count();

        model.addAttribute("activePage", "ticketCategory");
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("ticketCategoryListData", pageResult.getContent());
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("inactiveCount", inactiveCount);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResult.getTotalPages());

        return "backend/ticketCategory/listAllTicketCategory";
    }

    /* 進入新增門票頁 */
    @GetMapping("addTicketCategory")
    public String addTicketCategory(ModelMap model) {
        TicketCategoryVO ticketCategoryVO = new TicketCategoryVO();
        model.addAttribute("ticketCategoryVO", ticketCategoryVO);
        return "backend/ticketCategory/addTicketCategory";
    }

    /* 處理新增門票分類表單送出 (按下submit) */
    @PostMapping("insert")
    public String insert(@Valid TicketCategoryVO ticketCategoryVO,
            BindingResult result, ModelMap model) {
        if (result.hasErrors()) {
            // 以下是 html 檔案路徑
            return "backend/ticketCategory/addTicketCategory";
        }
        ticketCategoryService.addTicketCategory(ticketCategoryVO);
        // 以下是 網頁網址路徑
        return "redirect:/backend/ticketCategory/list";
    }

    // TODO: 討論：刪除將門類分類狀態改為2,而不是實際移除
    /* 處理刪除門票分類 (按下刪除) */
    @PostMapping("delete")
    public String delete(@RequestParam("ticketCategoryId") Integer ticketCategoryId, ModelMap model) {
        ticketCategoryService.deleteTicketCategory(ticketCategoryId);
        // 討論：是否採用非 redirect 作法
        return "redirect:/backend/ticketCategory/list";
    }

    /* 進入修改門票頁面 （將該id的VO資料導入進修改頁面） */
    @PostMapping("getOne_For_Update")
    public String getOne_For_Update(@RequestParam("ticketCategoryId") Integer ticketCategoryId, ModelMap model) {
        TicketCategoryVO ticketCategoryVO = ticketCategoryService.getOneTicketCategory(ticketCategoryId);
        model.addAttribute("ticketCategoryVO", ticketCategoryVO);
        // 回傳 HTML 檔案路徑
        return "backend/ticketCategory/updateTicketCategory";
    }

    /* 處理修改表單送出(按下submit) */
    @PostMapping("update")
    public String update(@Valid TicketCategoryVO ticketCategoryVO, BindingResult result, ModelMap model) {

        if (result.hasErrors()) {
            return "backend/ticketCategory/updateTicketCategory";
        }
        ticketCategoryService.updateTicketCategory(ticketCategoryVO);
        // 討論：是否採用非 redirect 作法
        return "redirect:/backend/ticketCategory/list";
    }

}