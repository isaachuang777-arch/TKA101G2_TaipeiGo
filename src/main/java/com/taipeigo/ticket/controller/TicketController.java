package com.taipeigo.ticket.controller;

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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.taipeigo.ticket.model.TicketSerialVO;
import com.taipeigo.ticket.model.TicketService;
import com.taipeigo.ticket.model.TicketVO;
import com.taipeigo.ticketcategory.model.TicketCategoryService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/backend/ticket")
public class TicketController {

    @Autowired
    TicketService ticketService;

    @Autowired
    private TicketCategoryService ticketCategoryService;

    /* 進入門票頁面 （查全部）[分頁 + 模糊搜尋] */
    @GetMapping("list")
    public String listAllTicket(
            ModelMap model,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "keyword", required = false) String keyword) {

        Page<TicketVO> pageResult;

        if (keyword != null && !keyword.trim().isEmpty()) {
            model.addAttribute("keyword", keyword.trim());
            pageResult = ticketService.searchTicketsByPage(keyword.trim(), page);
        } else {
            pageResult = ticketService.getAllTicketsByPage(page);
        }

        // 計算全域統計資訊 (總門票商品數、上架中、已下架)
        List<TicketVO> allTicketsList = ticketService.getAll();
        long totalCount = allTicketsList.size();
        long activeCount = allTicketsList.stream().filter(vo -> vo.getTicketStatus() == 1).count();
        long inactiveCount = allTicketsList.stream().filter(vo -> vo.getTicketStatus() == 0).count();

        model.addAttribute("activePage", "ticket");
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("ticketListData", pageResult.getContent());
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("inactiveCount", inactiveCount);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResult.getTotalPages());

        return "backend/ticket/listAllTicket";
    }

    /**
     * 查詢單筆門票詳細資料
     * 網址範例：/backend/ticket/getOne_For_Display?ticketId=1
     */
    @GetMapping("getOne_For_Display")
    public String getOneForDisplay(@RequestParam("ticketId") Integer ticketId, ModelMap model) {
        TicketVO ticketVO = ticketService.getOneTicket(ticketId);
        model.addAttribute("ticketVO", ticketVO);
        // 以下是 html 路徑
        return "backend/ticket/listOneTicket";
    }

    /**
     * RESTful API 查詢單筆門票 (回傳 JSON 格式)
     */
    /*
     * TODO: 後續 Thymeleaf 改前端
     * 
     * @GetMapping("/{ticketId}")
     * public ResponseEntity<TicketVO> getTicketJson(@PathVariable Integer ticketId)
     * {
     * TicketVO ticketVO = ticketService.getOneTicket(ticketId);
     * if (ticketVO == null) {
     * return ResponseEntity.notFound().build();
     * }
     * return ResponseEntity.ok(ticketVO);
     * }
     */

    /* 新增門票 */
    @GetMapping("/addTicket")
    public String addTicket(ModelMap model) {
        TicketVO ticketVO = new TicketVO();
        model.addAttribute("ticketVO", ticketVO);
        // 查出所有沒有被刪除的門票分類， th:each="category : ${categoryList}"
        model.addAttribute("categoryList", ticketCategoryService.getAllNotDeleted());
        // 以下是 html 路徑
        return "backend/ticket/addTicket";
    }

    /**
     * 接收表單提交，包含門票基本資料與 0~8 張的實體圖片檔案
     */
    @PostMapping("/insert")
    public String insert(
            @Valid TicketVO ticketVO,
            BindingResult result,
            ModelMap model,
            @RequestParam("ticketImageFiles") MultipartFile[] parts,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("categoryList", ticketCategoryService.getAllActive());
            return "backend/ticket/addTicket";
        }

        try {
            ticketService.addTicketWithImages(ticketVO, parts);
            redirectAttributes.addFlashAttribute("success", "成功新增門票商品：「" + ticketVO.getTicketName() + "」！");

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("categoryList", ticketCategoryService.getAllActive());
            model.addAttribute("errorMessage", "新增商品失敗：" + e.getMessage());
            return "backend/ticket/addTicket";
        }

        return "redirect:/backend/ticket/list";
    }

    /* 新增序號 */
    @PostMapping("/generateSerials") // 對應 th:action 網址
    public String generateSerials(
            @RequestParam("ticketId") Integer ticketId,
            @RequestParam("quantity") int quantity,
            RedirectAttributes redirectAttributes) {

        try {
            ticketService.generateSerials(ticketId, quantity);
            // 新增成功訊息
            redirectAttributes.addFlashAttribute("success", "成功為門票商品 (編號:" + ticketId + ") 新增了 " + quantity + " 張序號！");

        } catch (Exception e) {
            // 錯誤訊息
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "新增序號失敗：" + e.getMessage());
        }
        // 以下是 網頁網址路徑
        return "redirect:/backend/ticket/list";
    }

    /* 進入修改門票頁面 */
    @PostMapping("getOne_For_Update")
    public String getOne_For_Update(@RequestParam("ticketId") Integer ticketId, ModelMap model) {
        TicketVO ticketVO = ticketService.getOneTicket(ticketId);
        model.addAttribute("ticketVO", ticketVO);
        // 查出所有沒有被刪除的門票分類， th:each="category : ${categoryList}"
        model.addAttribute("categoryList", ticketCategoryService.getAllNotDeleted());
        return "backend/ticket/updateTicket";
    }

    /**
     * 送出修改確認
     * 網址對應：updateTicket.html 的 th:action="@{/ticket/update}"
     */
    @PostMapping("/update")
    public String update(
            @Valid TicketVO ticketVO,
            BindingResult result,
            @RequestParam("ticketImageFiles") MultipartFile[] parts,
            @RequestParam(value = "deleteImageIds", required = false) Integer[] deleteImageIds,
            ModelMap model,
            RedirectAttributes redirectAttributes) {

        // 如果驗證有錯，回原本的修改頁面時，代入原本的資料
        if (result.hasErrors()) {
            // 重新撈出門票分類
            model.addAttribute("categoryList", ticketCategoryService.getAllActive());

            // 重新撈出該門票原有的舊圖片重新存回 ticketVO
            TicketVO currentTicket = ticketService.getOneTicket(ticketVO.getTicketId());
            if (currentTicket != null) {
                ticketVO.setTicketImages(currentTicket.getTicketImages());
            }

            return "backend/ticket/updateTicket";
        }

        try {
            ticketService.updateTicketWithImages(ticketVO, parts, deleteImageIds);
            // 成功訊息
            redirectAttributes.addFlashAttribute("success", "門票商品 (編號:" + ticketVO.getTicketId() + ") 修改成功！");

        } catch (Exception e) {
            e.printStackTrace();
            // 儲存有異常，回原本頁面秀出紅字錯誤，要有原本的資料
            model.addAttribute("categoryList", ticketCategoryService.getAllActive());

            TicketVO currentTicket = ticketService.getOneTicket(ticketVO.getTicketId());
            if (currentTicket != null) {
                ticketVO.setTicketImages(currentTicket.getTicketImages());
            }

            model.addAttribute("errorMessage", "修改商品失敗：" + e.getMessage());
            return "backend/ticket/updateTicket";
        }

        // 修改成功，回門票明細頁
        return "redirect:/backend/ticket/getOne_For_Display?ticketId=" + ticketVO.getTicketId();
    }

    /* 進入門票頁面 （查全部）（分頁 + 模糊搜尋） */
    @GetMapping("/serialList")
    public String listAllTicketSerial(
            ModelMap model,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "keyword", required = false) String keyword) {

        Page<TicketSerialVO> pageResult;

        if (keyword != null && !keyword.trim().isEmpty()) {
            model.addAttribute("keyword", keyword.trim());
            pageResult = ticketService.searchTicketSerialsByPage(keyword.trim(), page);
        } else {
            pageResult = ticketService.getAllTicketSerialsByPage(page);
        }

        // 檢查過期狀態 + 更新 DB
        ticketService.checkAndUpdateExpiryList(pageResult.getContent());

        // 計算總門票序號數、庫存中、已售出
        List<TicketSerialVO> allSerialsList = ticketService.getAllTicketSerial();
        long totalCount = allSerialsList.size();
        long stockCount = allSerialsList.stream().filter(vo -> vo.getStatus() == 1).count();
        long soldCount = allSerialsList.stream().filter(vo -> vo.getStatus() == 2).count();

        model.addAttribute("activePage", "ticketSerial");
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("ticketSerialListData", pageResult.getContent());
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("stockCount", stockCount);
        model.addAttribute("soldCount", soldCount);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResult.getTotalPages());

        return "backend/ticket/listAllTicketSerial";
    }

    /* 下架序號 */
    @PostMapping("/offMarketSerial")
    public String offMarketSerial(@RequestParam("ticketSerialId") Integer ticketSerialId,
            RedirectAttributes redirectAttributes) {
        try {
            ticketService.offMarketTicketSerial(ticketSerialId);
            // redirectAttributes.addFlashAttribute("success", "序號 (編號: " + ticketSerialId +
            // ") 已成功下架！");
        } catch (Exception e) {
            // redirectAttributes.addFlashAttribute("error", "下架序號失敗：" + e.getMessage());
        }
        return "redirect:/backend/ticket/serialList";
    }
}