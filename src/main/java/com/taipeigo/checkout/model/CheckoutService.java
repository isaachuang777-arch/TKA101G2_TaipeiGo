package com.taipeigo.checkout.model;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taipeigo.cart.model.CartService;
import com.taipeigo.customer.model.CustomerVO;
import com.taipeigo.order.detail.model.OrderDetailService;
import com.taipeigo.orders.model.OrdersService;
import com.taipeigo.orders.model.OrdersVO;
import com.taipeigo.product.dto.CartItemDTO;
import com.taipeigo.product.model.ProductCartFacade;

import jakarta.servlet.http.HttpSession;

@Service
public class CheckoutService {

	@Autowired
	private CartService cartService;

	@Autowired
	private OrdersService ordersService;

	@Autowired
	private OrderDetailService orderDetailService;

	@Autowired
	private ProductCartFacade productCartFacade;
	
    @Transactional
	public void checkout(HttpSession session) {
		/**判斷會員是否登入~*/
		CustomerVO customer = (CustomerVO) session.getAttribute("loginCustomer");
		if (customer == null) {
		    throw new RuntimeException("請先登入");
		}
		/**取得購物車**/
		List<CartItemDTO> cartItems = cartService.queryCartItem(session);
		if (cartItems.isEmpty()) {
		    throw new RuntimeException("購物車沒有商品");
		}
		
		/***確認庫存***/
		for (CartItemDTO item : cartItems) {
		    boolean stock =	productCartFacade.checkStock(
		                    item.getProductType(),
		                    item.getProductId(),
		                    item.getQuantity());
		    if (!stock) {
		        throw new RuntimeException(item.getProductName() + " 庫存不足");
		    }
		}
		
		/**重新計算金額*/
		Integer total = 0;
		for (CartItemDTO item : cartItems) {
		    total += item.getSubtotal();
		}
		/**建立訂單,訂單明細**/
		OrdersVO order = ordersService.createOrder(customer.getCustId(),total);
		for (CartItemDTO item : cartItems) {
		    orderDetailService.createOrderDetail(order, item);
		    productCartFacade.checkoutItem(
		            item.getProductType(),
		            item.getProductId(),
		            item.getQuantity(),
		            customer.getCustId(),
		            order.getOrderId(),
		            item.getExpiryDate());

		}
		
		/**清空Redis**/
		cartService.clearCart(session);
	}

}
