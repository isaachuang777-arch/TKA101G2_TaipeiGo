package com.taipeigo.orders.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.taipeigo.customer.model.CustomerVO;
import com.taipeigo.order.detail.model.OrderDetailService;
import com.taipeigo.order.detail.model.OrderDetailVO;
import com.taipeigo.orders.model.OrdersService;
import com.taipeigo.orders.model.OrdersVO;

import jakarta.servlet.http.HttpSession;

@Controller
/* 凡是網址開頭是 /orders 的請求，都交給這個 Controller 處理 */
@RequestMapping("/frontend")

public class FrontCustOrdersController {
	@Autowired
	OrdersService ordersService;
	
	@Autowired
	OrderDetailService orderDetailService;
	
	/*****前台會員功能-訂單查詢****/
	@GetMapping("/customer/orders")
	public String customerOrders(HttpSession session, Model model) {
		CustomerVO loginCustomer =(CustomerVO) session.getAttribute("loginCustomer");
	    if (loginCustomer == null) {
	        return "redirect:/frontend/auth/login";
	    }
		Integer customerId=loginCustomer.getCustId();
		List<OrdersVO> orderList =    ordersService.getByCustId(customerId);
		    model.addAttribute("loginCustomer", loginCustomer);
		    model.addAttribute("orderList", orderList);
		    	return  "backend/orders/custOrders";
		
	}
	
	
	/*****前台會員功能-訂單明細查詢****/
	@GetMapping("/customer/orderDetail/{orderId}")
	public String customerOrderDetail(  
			@PathVariable Integer orderId,
            HttpSession session,
            Model model){
		/*從session抓到會員ID*/
		CustomerVO loginCustomer =(CustomerVO) session.getAttribute("loginCustomer");
        /*如果沒登入，就回到登入頁面*/
		if (loginCustomer == null) {
            return "redirect:/frontend/auth/login";
        }
		
        OrdersVO orders =ordersService.getOrdersId(orderId);
        if (orders == null) {
            return "redirect:/frontend/customer/orders";
        }
        
        /**防止會員透過URL去查詢別人的訂單*/
        if (!orders.getCustId().equals(loginCustomer.getCustId())) {
            return "redirect:/frontend/customer/orders";
        }

        List<OrderDetailVO> detailList =orderDetailService.findByOrderId(orderId);
        model.addAttribute("loginCustomer", loginCustomer);
        model.addAttribute("order", orders);
        model.addAttribute("detailList", detailList);
        return "backend/orders/custOrderDetail";
        
		
	}

}
