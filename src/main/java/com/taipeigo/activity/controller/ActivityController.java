package com.taipeigo.activity.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    // DTO 加入購物車用
    // @GetMapping("/{Id}/cartItem")
    // public CartItemDTO getCartItem(
    //         @PathVariable("Id") Integer id, @RequestParam("ticketType") String ticketType) {

    //     return activityService.getActivityCartItem(id, ticketType);
    // }

    @GetMapping("/categories")
    public List<ActivityCateVO> getAllCategories() {

        return activityService.getAllActiveCategories();
    }

    @GetMapping("/home-sections")
    public List<ActivitySectionDTO> getHomeSections() {

        return activityCateService.getHomeSections();
    }


    // 記得要在 Controller 最上面宣告並注入 Facade：
    // @Autowired
    // private ProductCartFacade productCartFacade;

    // ---------------- 以下是純測試用的暫時 API ----------------
    
    @GetMapping("/testTicketFacade")
    public ResponseEntity<?> testTicketFacade(@RequestParam Integer ticketId) {
        
        // 假設我們要買 2 張兒童票
        String type = "TICKET";
        Integer quantity = 2;
        String spec = "CHILD";
        
        // 1. 測試：呼叫 Facade 檢查門票庫存
        boolean hasStock = productCartFacade.checkStock(type, ticketId, quantity);
        
        // 2. 測試：呼叫 Facade 取得門票 DTO
        CartItemDTO dto = productCartFacade.getCartItemInfo(type, ticketId, quantity, spec);
        
        // 3. 把結果包裝成 JSON 顯示在網頁上看看
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("是否有足夠庫存", hasStock);
        result.put("Facade自動幫Ticket產生的DTO", dto);
        
        return ResponseEntity.ok(result);
    }

    // ---------------- 測試活動用的 API ----------------
    
    @GetMapping("/testActivityFacade")
    public ResponseEntity<?> testActivityFacade(@RequestParam Integer activityId) {
        
        // 假設我們要報名 2 個成人名額的活動
        String type = "ACTIVITY";
        Integer quantity = 1;
        String spec = "ADULT";
        
        // 1. 測試：呼叫 Facade 檢查活動名額夠不夠（會連動去檢查門票喔！）
        boolean hasStock = productCartFacade.checkStock(type, activityId, quantity);
        
        // 2. 測試：呼叫 Facade 取得活動 DTO (會觸發你辛苦寫的動態價格與手續費邏輯)
        CartItemDTO dto = productCartFacade.getCartItemInfo(type, activityId, quantity, spec);
        
        // 3. 把結果包裝成 JSON 顯示在網頁上看看
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("套票數量是否足夠", hasStock);
        result.put("Facade自動幫Activity產生的DTO", dto);
        
        return ResponseEntity.ok(result);
    }

}
