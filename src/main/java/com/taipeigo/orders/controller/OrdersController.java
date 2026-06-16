package com.taipeigo.orders.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.taipeigo.order.detail.model.OrderDetailService;
import com.taipeigo.orders.model.OrdersService;
import com.taipeigo.orders.model.OrdersVO;


@Controller
/*凡是網址開頭是 /orders 的請求，都交給這個 Controller 處理*/
//**********
@RequestMapping("/orders")

public class OrdersController {
	@Autowired
	OrdersService ordersService;

	@Autowired
	OrderDetailService orderDetailService;

	/* orders首頁--查全部 */
	@GetMapping
	public String getAllOrders(ModelMap model) {

		List<OrdersVO> allOrders = ordersService.getAll();
		model.addAttribute("allOrders", allOrders);
		return "backend/orders/allOrders";

	}

}
