package com.taipeigo.activity.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.taipeigo.activity.model.ActivityService;
import com.taipeigo.activity.model.ActivityVO;

@Controller
@RequestMapping("/backend/activities")
public class ActivityBackendController {

    private final ActivityService activityService;

    @Autowired
    public ActivityBackendController(ActivityService activityService) {

        this.activityService = activityService;
    }

    @GetMapping
    public String getBackendActivitiesPage(@RequestParam MultiValueMap<String, String> params, Model model) {

        List<ActivityVO> activityList = activityService.getBackendActivitiesByCompositeQuery(params);

        model.addAttribute("activityList", activityList);

        return "backend/activity/list";

    }

}
