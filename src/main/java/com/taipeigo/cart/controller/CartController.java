package com.taipeigo.cart.controller;

import java.time.LocalDateTime;
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
import com.taipeigo.product.dto.CartItemDTO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/frontend/cart")
public class CartController {
	@Autowired
	CartService cartService;

	@Autowired(required = true)
	private RedisTemplate<String, Object> redisTemplate;

	@GetMapping("")
	public String cartHome() {
		return "redirect:/frontend/cart/shoppingCart";
	}
/*______★☆★★☆★★☆★★☆★☆★☆測試資料*/
	@GetMapping("/testInsert")
	@ResponseBody
	public String testInsert(HttpSession session){

	    CartVO cart = new CartVO();
	    cart.setProductId(1);
	    cart.setProductType("TICKET");
	    cart.setProductQuantity(2);
	    cart.setExpiryDate(LocalDateTime.parse("2026-07-01T00:00:00"));
	    cart.setSpec("ADULT");

	    cartService.insertCart(cart, session);

	    return "OK";
	}
/*______★☆★★☆★★☆★★☆★☆★☆測試資料*/

	@GetMapping("/shoppingCart")
	public String shoppingCart(HttpSession session, Model model) {
		/** 進入購物車前，先判斷是否登入，若未登入，就跳轉至登入頁 **/
		CustomerVO loginCustomer = (CustomerVO) session.getAttribute("loginCustomer");
		if (loginCustomer == null) {
			return "redirect:/auth/login";
		}
		/** 若已登入，就跳轉至購物車頁面 **/
		model.addAttribute("loginCustomer", loginCustomer);
		return "frontend/cart/shoppingCart";
	}

	/* ========== 查詢購物車功能==>查的是購物車原始資料 */
	@GetMapping("/queryCart")
	@ResponseBody
	public List<CartVO> queryCart(HttpSession session) {
		return cartService.queryCart(session);
	} 

	/* ========== 查詢傳進來商品的購物車功能==>前端畫面傳進來的資料 */
	@GetMapping("/queryCartDetail")
	@ResponseBody
	public List<CartItemDTO> queryCartDetail(HttpSession session){
		return cartService.queryCartItem(session);
	}
	
	/* ========== 加入購物車功能 */
	@PostMapping("/insertCart")
	@ResponseBody
	public String insertCart(@RequestBody CartVO cartVO, 
			HttpSession session) {
		/*先判斷是否登入*/
		System.out.println("========== insertCart ==========");
		System.out.println(cartVO);
	    CustomerVO loginCustomer = (CustomerVO) session.getAttribute("loginCustomer");
	    if (loginCustomer == null) {
	        cartService.insertCart(cartVO, session);
	        /*回傳CustomerNEEDlogin讓活動, 門票知道先將葉面引導至登入頁 */
	        return "CustomerNEEDlogin";
	    }

	    cartService.insertCart(cartVO, session);
	    return "success";
	}
	

	/* ========== 更新購物車功能 */
	@PostMapping("/updateCart")
	@ResponseBody
	public String updateCart(@RequestBody CartVO cartVO, HttpSession session) {
		cartService.updateCart(cartVO, session);
		return "success";
	}

	/* ========== 購物車刪除門票功能 */
	@PostMapping("/removeCartProduct")
	@ResponseBody
	public String removeCartProduct(@RequestBody CartVO cartVO, HttpSession session) {
		cartService.removeProduct(cartVO, session);
		return "success";
	}

	/* ========== 購物車刪除門票功能 */
	@DeleteMapping("/clearCart")
	@ResponseBody
	public String clearCart(HttpSession session) {
	    System.out.println("========== clearCart ==========");

		cartService.clearCart(session);
		return "success";
	}

	/* ========== 購物車icon數量顯示功能 */
	@GetMapping("/count")
	@ResponseBody
	public Integer countCart(HttpSession session) {

		return cartService.countCart(session);

	}

}
