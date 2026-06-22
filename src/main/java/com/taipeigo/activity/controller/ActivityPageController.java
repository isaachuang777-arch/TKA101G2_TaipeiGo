package com.taipeigo.activity.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// 空殼Activity 活動頁面 核心API放在ActivityController(RESTful寫法)
@Controller
public class ActivityPageController {

    @GetMapping("/activity")
    public String showActivityPage() {

        return "frontend/activity/activity";
    }

}
