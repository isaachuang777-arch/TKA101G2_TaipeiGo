package com.taipeigo.order.detail.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.taipeigo.order.detail.model.OrderDetailService;
import com.taipeigo.order.detail.model.OrderDetailVO;
import com.taipeigo.orders.model.OrdersService;

@Controller
/* 凡是網址開頭是 /orderDetail 的請求，都交給這個 Controller 處理 */
@RequestMapping("/orderDetail")
public class OrderDetailController {

	@Autowired
	OrdersService ordersService;

	@Autowired
	OrderDetailService orderDetailService;

	/* orders首頁--查全部 */
	@GetMapping("/{orderId}")
	public String orderId(@PathVariable Integer orderId, Model model) {

		List<OrderDetailVO> details = orderDetailService.findByOrderId(orderId);
		model.addAttribute("details", details);
		return "backend/orders/orderDetail";

	}

}
