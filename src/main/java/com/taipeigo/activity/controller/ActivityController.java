package com.taipeigo.activity.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taipeigo.activity.model.ActivityCateService;
import com.taipeigo.activity.model.ActivityCateVO;
import com.taipeigo.activity.model.ActivitySectionDTO;
import com.taipeigo.activity.model.ActivityService;
import com.taipeigo.activity.model.ActivityVO;
import com.taipeigo.product.dto.CartItemDTO;
import com.taipeigo.product.model.ProductCartFacade;

@RestController
@RequestMapping("/activities")
public class ActivityController {

    private final ActivityService activityService;
    private final ActivityCateService activityCateService;
    private final ProductCartFacade productCartFacade;

    @Autowired
    public ActivityController(ActivityService activityService, 
                              ActivityCateService activityCateService, 
                              ProductCartFacade productCartFacade) {

        this.activityService = activityService;
        this.activityCateService = activityCateService;
        this.productCartFacade = productCartFacade;

    }

    // 萬用搜尋API

    @GetMapping
    public List<ActivityVO> getAllActivities(@RequestParam MultiValueMap<String, String> params) {

        List<ActivityVO> list = activityService.getActivitiesByCompositeQuery(params);

        return list;

    }

    // 單一搜尋API

    @GetMapping("/{id}")
    public ActivityVO getActivityById(@PathVariable("id") Integer id) {

        return activityService.getActivityVOById(id);
    }

    //DTO 加入購物車用
    @GetMapping("/{Id}/cartItem")
    public CartItemDTO getCartItem(
            @PathVariable("Id") Integer id, 
            @RequestParam(value = "quantity", defaultValue = "1") Integer quantity, 
            @RequestParam("ticketType") String ticketType) {

        return activityService.getActivityCartItem(id, quantity, ticketType);
    }

    @GetMapping("/categories")
    public List<ActivityCateVO> getAllCategories() {

        return activityService.getAllActiveCategories();
    }

    @GetMapping("/home-sections")
    public List<ActivitySectionDTO> getHomeSections() {

        return activityCateService.getHomeSections();
    }


}
