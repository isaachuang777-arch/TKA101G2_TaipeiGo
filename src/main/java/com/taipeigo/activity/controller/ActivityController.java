package com.taipeigo.activity.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.taipeigo.activity.model.ActivityDetailVO;
import com.taipeigo.activity.model.ActivitySectionDTO;
import com.taipeigo.activity.model.ActivityService;
import com.taipeigo.activity.model.ActivityVO;
import com.taipeigo.cart.model.CartService;
import com.taipeigo.cart.model.TicketStockDTO;
import com.taipeigo.product.dto.CartItemDTO;
import com.taipeigo.product.model.ProductCartFacade;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/activities")
public class ActivityController {

    private final ActivityService activityService;
    private final ActivityCateService activityCateService;
    private final ProductCartFacade productCartFacade;
    private final CartService cartService;

    @Autowired
    public ActivityController(ActivityService activityService, 
                              ActivityCateService activityCateService, 
                              ProductCartFacade productCartFacade,
                              CartService cartService) {

        this.activityService = activityService;
        this.activityCateService = activityCateService;
        this.productCartFacade = productCartFacade;
        this.cartService = cartService;

    }

    // 萬用搜尋API

    @GetMapping
    public List<ActivityVO> getAllActivities(@RequestParam MultiValueMap<String, String> params) {

        List<ActivityVO> list = activityService.getActivitiesByCompositeQuery(params);

        return list;

    }


    @GetMapping("/total-pages")
    public int getTotalPage(@RequestParam MultiValueMap<String, String> params){

        //直接拿查詢出來的分頁邏輯來用
         return activityService.getTotalPageByCompositeQuery(params);
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

    // 檢查活動庫存是否充足

    @GetMapping("/checkStock")
    public boolean checkStock(
        @RequestParam("activityId") Integer activityId,
        @RequestParam("quantity") Integer quantity) {

             return productCartFacade.checkStock("ACTIVITY", activityId, quantity);
        }
    
    @GetMapping("/checkStockWithCart")
    public ResponseEntity<Boolean> checkStockWithCart(
        @RequestParam("activityId") Integer activityId,
        @RequestParam("quantity") Integer quantity,
        HttpSession session){
        
            try{

                // 用Cart組員寫好的API拿到所有ticket被占用的數量
                List<TicketStockDTO> cartTickets = cartService.ticketIdQuantitySearch(session);

                // 轉成 Map 比較好找(TicketId -> 已佔用的數量)
                Map<Integer, Integer> occupiedMap = new HashMap<>();
                if(cartTickets != null){

                    for(TicketStockDTO dto : cartTickets){

                        occupiedMap.put(dto.getTicketId(), dto.getQuantity());
                    }
                }

                // 找出這個 Activity 裡面包含哪些 Ticket

                ActivityVO activity = activityService.getActivityVOById(activityId);

                for(ActivityDetailVO detail : activity.getActivityDetails()){

                    Integer ticketId = detail.getTicket().getTicketId();

                    // 購物車占用的數量，沒有的話就是0

                    int occupied = occupiedMap.getOrDefault(ticketId, 0);

                    // 總數 = 購物車的數量加上這次想新增的數量
                    int totalWanted = occupied + quantity;

                    // 去TicketService問庫存有沒有大於totalWanted

                    if(!productCartFacade.checkStock("TICKET",ticketId, totalWanted)){

                        return ResponseEntity.ok(false); 
                    }
                }

                return ResponseEntity.ok(true);

            } catch (Exception e){

                e.printStackTrace();
                return ResponseEntity.ok(false);
            }

        }




}
