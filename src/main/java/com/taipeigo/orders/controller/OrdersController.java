package com.taipeigo.orders.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.ModelAndView;

import com.taipeigo.order.detail.model.OrderDetailService;
import com.taipeigo.orders.model.OrdersService;
import com.taipeigo.orders.model.OrdersVO;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

@Controller
/* 凡是網址開頭是 /orders 的請求，都交給這個 Controller 處理 */
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
	

	/* order 訂單編號查詢 Search OrderId */
	@PostMapping("search_ordersId")
	public String search_ordersId(/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 *************************/
	@NotEmpty(message = "訂單編號: 請勿空白") 
	@Min(value = 1, message = "訂單編號不能小於{value}") 
	@Max(value = 999999, message = "訂單編號不能超過{value}") 
	@RequestParam("ordersId") String ordersId,
			ModelMap model) {

		/* 查詢ID邏輯 */
		OrdersVO ordersVO = ordersService.getOrdersId(Integer.valueOf(ordersId));
		if (ordersVO == null) {
			model.addAttribute("errorMessage", "查無資料");
			List<OrdersVO> allOrders = ordersService.getAll();
			model.addAttribute("allOrders", allOrders);
			return "backend/orders/allOrders";
		}
		/* 如果不是空值，就顯示該筆 */
		List<OrdersVO> resultOrderId = new ArrayList<>();
		resultOrderId.add(ordersVO);
		model.addAttribute("allOrders", resultOrderId);

		return "backend/orders/allOrders";
	}

	/* order 會員編號查詢  Search CustomerId */
	@PostMapping("search_custId")
	public String search_custId(
			/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 *************************/
			@NotEmpty(message = "會員編號: 請勿空白") 
			@Min(value = 1, message = "會員編號不能小於{value}") 
			@Max(value = 999999, message = "會員編號不能超過{value}") 
			@RequestParam("custId") String custId,
			ModelMap model) {
		/* 查詢ID邏輯 */
		List<OrdersVO> list = ordersService.getByCustId(Integer.valueOf(custId));
		if (list.isEmpty()) {
			/*如果找不到，就顯示"查無資料"，且找全部*/
			model.addAttribute("errorMessage", "查無資料");
			List<OrdersVO> allOrders = ordersService.getAll();
			model.addAttribute("allOrders", allOrders);
			return "backend/orders/allOrders";
		}
		model.addAttribute("allOrders", list);

		return "backend/orders/allOrders";
	}

	
	
	
	
	
	
	
	
	
	
	
	/*** 異常處理器: for 「方法級別驗證」報錯用 ****/
	@ExceptionHandler(value = { HandlerMethodValidationException.class, ConstraintViolationException.class })
	public ModelAndView handleError(Exception e, Model model) {

		StringBuilder strBuilder = new StringBuilder();

		if (e instanceof HandlerMethodValidationException ex) {

			ex.getParameterValidationResults().forEach(result -> {
				result.getResolvableErrors()
						.forEach(error -> strBuilder.append(error.getDefaultMessage()).append("<br>"));
			});

		} else if (e instanceof ConstraintViolationException ex) {

			ex.getConstraintViolations().forEach(violation -> strBuilder.append(violation.getMessage()).append("<br>"));
		}

		List<OrdersVO> allOrders = ordersService.getAll();
		model.addAttribute("allOrders", allOrders);

		return new ModelAndView("backend/orders/allOrders", "errorMessage", "請修正以下錯誤：<br>" + strBuilder.toString());

	}
}
