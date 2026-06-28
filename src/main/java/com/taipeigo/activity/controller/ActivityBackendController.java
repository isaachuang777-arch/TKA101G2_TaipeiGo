package com.taipeigo.activity.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.taipeigo.activity.model.ActivityService;
import com.taipeigo.activity.model.ActivityVO;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/backend/activity")
public class ActivityBackendController {

    private final ActivityService activityService;

    @Autowired
    public ActivityBackendController(ActivityService activityService) {

        this.activityService = activityService;
    }

    // 讓所有此 Controller 的頁面都能拿到 activityCateList (為了左側搜尋欄下拉選單)
    @ModelAttribute("activityCateList")
    public List<com.taipeigo.activity.model.ActivityCateVO> getCategories() {
        return activityService.getAllActiveCategories();
    }

    // 讓所有此 Controller 的頁面都能拿到所有門票清單 (供前端選取明細)
    @ModelAttribute("ticketList")
    public List<com.taipeigo.ticket.model.TicketVO> getTicketList() {
        return activityService.getAllTickets();
    }



    // 萬用查詢列表

    @GetMapping
    public String getBackendActivitiesPage(@RequestParam MultiValueMap<String, String> params, Model model) {

        List<ActivityVO> activityList = activityService.getBackendActivitiesByCompositeQuery(params);

        // 總頁數
        int totalPage = activityService.getTotalPageByCompositeQuery(params);

        //抓出算好的頁數 - 三元判斷 沒有的話就 1
        int currentPage = params.containsKey("page") ? Integer.parseInt(params.get("page").get(0)) : 1;

        // 包給HTML
        model.addAttribute("activityList", activityList);
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("currentPage", currentPage);

        // 把前端的params在傳回去 避免換頁的時候消失
        model.addAttribute("queryParams", params);
        
        return "backend/activity/list";

    }

    // 前往新增頁面，先用GetMapping做一個空的ActivityVO

    @GetMapping("/add")
    public String showAddPage(Model model) {

        model.addAttribute("activityVO", new ActivityVO());

        return "backend/activity/add";
    }

    // 將前端資料塞進去ActivityVO並呼叫Service存進資料庫

    @PostMapping("/add")
    public String processAddActivity(
            @Valid @ModelAttribute("activityVO") ActivityVO activityVO,
            BindingResult result,
            @RequestParam(value = "ticketIds", required = false) List<Integer> ticketIds,
            @RequestParam(value = "upFiles", required = false) MultipartFile[] images,
            @RequestParam(value = "cateIds", required = false) List<Integer> cateIds,
            Model model) {

        if (result.hasErrors()) {

            return "backend/activity/add";
        }

        try {
            activityService.addActivity(activityVO, ticketIds, images, cateIds);
        } catch (Exception e) {

            model.addAttribute("errorMessage", "新增失敗" + e.getMessage());

            return "backend/activity/add";
        }

        return "redirect:/backend/activity";
    }

    // 前往修改頁面

    @GetMapping("/{id}/edit")
    public String showEditPage(@PathVariable("id") Integer id, Model model) {

        ActivityVO activityVO = activityService.getActivityVOById(id);

        model.addAttribute("activityVO", activityVO);

        return "backend/activity/edit";
    }

    // 將前端資料傳進service判斷修改存檔

    @PostMapping("/{id}/edit")
    public String processUpdateActivity(

            @PathVariable("id") Integer id,
            @Valid @ModelAttribute("activityVO") ActivityVO activityVO,
            BindingResult result,
            @RequestParam(value = "ticketIds", required = false) List<Integer> ticketIds,
            @RequestParam(value = "upFiles", required = false) MultipartFile[] images, 
            @RequestParam(value = "deleteImageIds", required = false) List<Integer> deleteImageIds,
            @RequestParam(value = "cateIds", required = false) List<Integer> cateIds,
            Model model) {

        if (result.hasErrors()) {

            return "backend/activity/edit";
        }
        
        System.out.println("====== DEBUG: deleteImageIds received: " + deleteImageIds + " ======");

        try {

            activityService.updateActivity(id, activityVO, ticketIds, images, deleteImageIds, cateIds);
        }

        catch (Exception e) {

            model.addAttribute("errorMessage", "修改失敗: " + e.getMessage());

            return "backend/activity/edit";
        }

        return "redirect:/backend/activity";
    }


    @PostMapping("/{id}/status")
    @ResponseBody
    public ResponseEntity<String> updateStatus(

        @PathVariable("id") Integer id,
        @RequestParam("status") Integer status ){

            try{
                
                activityService.updateActivityStatus(id, status);
                return ResponseEntity.ok("狀態更新成功");

            } catch (Exception e){

                return ResponseEntity.badRequest().body("狀態更新失敗: " + e.getMessage());
                
            }
            
        }

}
