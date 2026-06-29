package com.taipeigo.checkout.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.taipeigo.cart.model.CartService;
import com.taipeigo.checkout.model.CheckoutService;
import com.taipeigo.customer.model.CustomerVO;
import com.taipeigo.product.dto.CartItemDTO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/frontend/checkout")

public class CheckoutController {
	@Autowired
	private CheckoutService checkoutService;
	
	@Autowired
	private CartService cartService;

	@GetMapping
	/**結帳頁面載入*/
	public String checkoutPage(
			Model model,
			HttpSession session) {
		/**確認是否登入**/
	    CustomerVO customer = (CustomerVO) session.getAttribute("loginCustomer");
	    if (customer == null) {
	        return "redirect:/auth/login";
	    }

	    model.addAttribute("loginCustomer", customer);

	    /**取得購物車商品**/
	    List<CartItemDTO> cartItems = cartService.queryCartItem(session);
	    model.addAttribute("cartItems", cartItems);

	    /**訂單摘要**/
	    int totalCount = 0;
	    int totalAmount = 0;
	    for (CartItemDTO item : cartItems) {
	        totalCount += item.getQuantity();
	        totalAmount += item.getSubtotal();
	    }
	    model.addAttribute("totalCount", totalCount);
	    model.addAttribute("totalAmount", totalAmount);
	    return "frontend/checkout/checkout";

	}
	
	
	
	/** 付款 API*/
	@ResponseBody
	@PostMapping("/pay")
	public ResponseEntity<String> pay(HttpSession session) {
	    checkoutService.checkout(session);
	    return ResponseEntity.ok("success");
	}
}
