package com.taipeigo.activity.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.multipart.MultipartFile;

import com.taipeigo.activity.model.ActivityService;
import com.taipeigo.activity.model.ActivityVO;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/backend/activities")
public class ActivityBackendController {

    private final ActivityService activityService;

    @Autowired
    public ActivityBackendController(ActivityService activityService) {

        this.activityService = activityService;
    }

    // 萬用查詢列表

    @GetMapping
    public String getBackendActivitiesPage(@RequestParam MultiValueMap<String, String> params, Model model) {

        List<ActivityVO> activityList = activityService.getBackendActivitiesByCompositeQuery(params);

        model.addAttribute("activityList", activityList);

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
            Model model) {

        if (result.hasErrors()) {

            return "backend/activity/add";
        }

        try {
            activityService.addActivity(activityVO, ticketIds, images);
        } catch (Exception e) {

            model.addAttribute("errorMessage", "新增失敗" + e.getMessage());

            return "backend/activity/add";
        }

        return "redirect:/backend/activities";
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
            @RequestParam(value = "upFiles", required = false) MultipartFile[] images, Model model) {

        if (result.hasErrors()) {

            return "backend/activity/edit";
        }

        try {

            activityService.updateActivity(id, activityVO, ticketIds, images);
        }

        catch (Exception e) {

            model.addAttribute("errorMessage", "修改失敗: " + e.getMessage());

            return "backend/activity/edit";
        }

        return "redirect:/backend/activities";
    }

}
