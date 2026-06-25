package com.taipeigo.cart.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.taipeigo.cart.model.CartService;
import com.taipeigo.cart.model.CartVO;
import com.taipeigo.customer.model.CustomerVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/frontend/cart")
public class CartController {
	@Autowired
	CartService cartService;

	@Autowired(required = false)
	private RedisTemplate<String, Object> redisTemplate;
	
	@GetMapping("")
	public String cartHome() {
	    return "redirect:/frontend/cart/shoppingCart";
	}
	
	@GetMapping("/shoppingCart")
	public String shoppingCart(
			HttpSession session, 
			Model model) {
		/**進入購物車前，先判斷是否登入，若未登入，就跳轉至登入頁**/
		CustomerVO loginCustomer =(CustomerVO) session.getAttribute("loginCustomer");
		if (loginCustomer == null) {
			return "redirect:/frontend/auth/login";
		}
		/**若已登入，就跳轉至購物車頁面**/
		model.addAttribute("loginCustomer", loginCustomer);
		return "frontend/cart/shoppingCart";
	}
	
	

/*========== 查詢購物車功能 */
	@GetMapping("/queryCart")
	@ResponseBody
	public  List<CartVO>  queryCart(HttpSession session) {
	    return cartService.queryCart(session);
	}
	
	

/*==========  加入購物車功能 */
	@PostMapping("/insertCart")
	@ResponseBody
	public String insertCart(  
			@RequestBody CartVO cartVO,
	        HttpSession session) {
	    cartService.insertCart(cartVO, session);

	    return "success";

	}

/*========== 更新購物車功能 */
	@PostMapping("/updateCart")
	@ResponseBody
	public String updateCart(
			@RequestBody CartVO cartVO,
	        HttpSession session) {
	    cartService.updateCart(cartVO, session);

	    return "success";
	}

/*==========  購物車刪除門票功能 */
	@PostMapping("/removeCartProduct")
	@ResponseBody
	public String removeCartProduct(
			@RequestBody CartVO cartVO,
	        HttpSession session) {
	    cartService.removeProduct(cartVO, session);
	    return "success";
	}

/*==========  購物車刪除門票功能 */
	@DeleteMapping("/clearCart")
	@ResponseBody
	public String clearCart(HttpSession session) {
		cartService.clearCart(session);
		return "success";
	}

/*========== 購物車icon數量顯示功能 */
	@GetMapping("/count")
	@ResponseBody
	public Integer countCart(HttpSession session) {
		return cartService.countCart(session);
		
	}

	
	
}
