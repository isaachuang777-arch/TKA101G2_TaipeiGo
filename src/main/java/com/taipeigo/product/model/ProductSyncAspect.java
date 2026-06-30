package com.taipeigo.product.model;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.taipeigo.ticket.model.TicketVO;
import com.taipeigo.activity.model.ActivityVO;

@Aspect
@Component
public class ProductSyncAspect {

    private final ProductService productService;


    @Autowired
    public ProductSyncAspect(ProductService productService){
        this.productService = productService;
    }

        // 偵測Ticket，只要ticket有save就將新存的ticketVO存到savedTicket，然後呼叫syncTicketToProduct
        // 並把savedTicket存到product table

        @AfterReturning(
            pointcut = "execution(* com.taipeigo.ticket.model.TicketRepository.save(..))",
            returning = "savedTicket"
        )
        public void syncTicketAfterSave(Object savedTicket){

            if(savedTicket instanceof TicketVO){
                productService.syncTicketToProduct((TicketVO) savedTicket);
            }
        }

        //同上，但是是處理Activity

        @AfterReturning(
            pointcut = "execution(* com.taipeigo.activity.model.ActivityRepository.save(..))",
            returning = "savedActivity"
        )
        public void syncActivityAfterSave(Object savedActivity){

            if(savedActivity instanceof ActivityVO){

                productService.syncActivityToProduct((ActivityVO) savedActivity);
            }
        }
    
}