package com.taipeigo.ticketcategory.controller;


import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/ticketCategory")
public class TicketCategoryController {

    @Autowired
    TicketCategoryService ticketCategoryService;
    
    
    /* 進入票券種類列表頁面 (查全部) */
    @GetMapping("listAllTicketCategory")
    public String listAllCategory(ModelMap model) {
        List<TicketCategoryVO> list = ticketCategoryService.getAll();
        model.addAttribute("ticketCategoryListData", list); 
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
        return "redirect:/ticketCategory/listAllTicketCategory";
    }
    
    // TODO: 討論：是否刪除還是要保留ID => 資料庫增加 is_deleted 欄位
    /* 處理刪除門票分類 (按下刪除)*/
    @PostMapping("delete")
    public String delete(@RequestParam("ticketCategoryId") Integer ticketCategoryId, ModelMap model) {
        ticketCategoryService.deleteTicketCategory(ticketCategoryId); 
        // 討論：是否採用非 redirect 作法
        return "redirect:/ticketCategory/listAllTicketCategory";
    }
    
    /* 進入修改門票頁面 （將該id的VO資料導入進修改頁面）*/
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
        return "redirect:/ticketCategory/listAllTicketCategory";
    }

}