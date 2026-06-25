package com.taipeigo.product.model;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taipeigo.activity.model.ActivityService;
import com.taipeigo.product.dto.CartItemDTO;
import com.taipeigo.ticket.model.TicketService;
import com.taipeigo.ticket.model.TicketVO;

// product 萬用 api, 不管是ticket 或 activity都可以拿這隻來送購物車前端畫面跟資料過去
// 也包含了檢查庫存

@Service
public class ProductCartFacade {

     private final ActivityService activityService;

     private final TicketService ticketService;

    @Autowired
    public ProductCartFacade(ActivityService activityService, TicketService ticketService){

        this.activityService = activityService;

        this.ticketService = ticketService;
    }

    // 購物車顯示用的
    public CartItemDTO getCartItemInfo(String productType, Integer productId, Integer quantity, String spec){

        if("ACTIVITY".equalsIgnoreCase(productType)){

            return activityService.getActivityCartItem(productId, quantity, spec);
        } 
        
        // ticket 用的 跟activity不同 ticket其他組員的東西這邊直接幫他包dto成
        else if ("TICKET".equalsIgnoreCase(productType)){

            TicketVO ticketVO = ticketService.getOneTicket(productId);

            if(ticketVO == null) {

                throw new RuntimeException("找不到該門票");
            }

             CartItemDTO dto = new CartItemDTO();
             dto.setProductId(ticketVO.getTicketId());
             dto.setProductType("TICKET");
             dto.setSpec(spec);
             dto.setQuantity(quantity);


            if ("CHILD".equalsIgnoreCase(spec)){

                dto.setProductName(ticketVO.getTicketName() + "(兒童票)");
                dto.setPrice(ticketVO.getChildPrice());
            } 
            
            else if ("CONCESSION".equalsIgnoreCase(spec)){

                dto.setProductName(ticketVO.getTicketName() + " (優待票)");
                dto.setPrice(ticketVO.getConcessionPrice());

            } 

            else {
                
                dto.setProductName(ticketVO.getTicketName() + " (成人票)");
                dto.setPrice(ticketVO.getAdultPrice());

            }

            // 計算小記
            if(dto.getPrice() != null) {

                dto.setSubtotal(dto.getPrice()* quantity);

            }

            // 抓門票圖片
            if(ticketVO.getTicketImages() != null && !ticketVO.getTicketImages().isEmpty()){

                dto.setImageUrl(ticketVO.getTicketImages().get(0).getTicketImageSrc());
            }

            return dto;

        }

        throw new IllegalArgumentException("未知的商品類型: " + productType);
    }


    // 加入購物車前用的：檢查庫存
    public boolean checkStock(String productType, Integer productId, Integer quantity){

        if ("ACTIVITY".equalsIgnoreCase(productType)) {

            // 我自己版本的checkStock(拿ticketService的checkStock魔改)

            return activityService.checkStock(productId, quantity);
            
        } 

        else if ("TICKET".equalsIgnoreCase(productType)){

            // 用ticketService的checkStock方法處理就好

             return ticketService.checkStock(productId, quantity);
        }

        return false;

    }

    // 結帳最後一步用的:扣除庫存並建立訂單綁定
    public void checkoutItem(String productType, Integer productId,
                             Integer quantity, Integer custId, 
                             Integer orderId, LocalDateTime expiryDate) {

        if ("ACTIVITY".equalsIgnoreCase(productType)) {
        
        // 用我自己的buyActivity方法(用buyTicketSerial魔改)
         activityService.buyActivity(productId, quantity, custId, orderId);
        } 

    else if ("TICKET".equalsIgnoreCase(productType)){

        // 將 LocalDateTime 轉換為 Timestamp 供 TicketService 使用
        java.sql.Timestamp sqlExpiryDate = null;
        if (expiryDate != null) {
            sqlExpiryDate = java.sql.Timestamp.valueOf(expiryDate);
        }

        // ticketService的buyTicketSerial拿來用，但這方法一次只能處理一張，跑迴圈方式去處理

        for(int i = 0; i < quantity; i++){

             ticketService.buyTicketSerial(productId, sqlExpiryDate, custId, orderId); 
        }
    } 

    }


}
