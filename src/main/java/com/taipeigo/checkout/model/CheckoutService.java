package com.taipeigo.checkout.model;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taipeigo.cart.model.CartService;
import com.taipeigo.cart.model.CartVO;
import com.taipeigo.cart.model.TicketStockDTO;
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
	
	@Autowired
	private CheckoutRepository checkoutRepository;
	
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
    

 /* ========== 載入結帳頁前，確認庫存並自動調整購物車 ========== */
    public void checkoutStockCheck(HttpSession session) {
    	boolean cartChanged = false;
        System.out.println("========== checkoutStockCheck Service ==========");

        /*取得 Redis 購物車*/
        List<CartVO> cartList = cartService.queryCart(session);
        if (cartList.isEmpty()) {
            return;
        }
        
        /**取得購物車所有 Ticket 的占用數量 (Ticket + Activity)**/
        List<TicketStockDTO> ticketList = cartService.ticketIdQuantitySearch(session);
        Map<Integer, Integer> occupiedMap = new HashMap<>();
        for (TicketStockDTO dto : ticketList) {
            occupiedMap.put(dto.getTicketId(), dto.getQuantity());
        }
        /***比對每個 Ticket 是否超過庫存**/
        for (Map.Entry<Integer, Integer> entry : occupiedMap.entrySet()) {
            Integer ticketId = entry.getKey();          // TicketId
            Integer occupied = entry.getValue();        // 購物車目前總數
            Integer stock = checkoutRepository.getAvailableStock(ticketId);
            System.out.println("========================");
            System.out.println("ticketId = " + ticketId);
            System.out.println("購物車占用 = " + occupied);
            System.out.println("資料庫庫存 = " + stock);
            /***如果真的爆了，扣除順序:ticketId==>日期最遠==>日期相同==>Adult==>Child==>Concession***/
            if (occupied > stock) {
            	cartChanged = true;
                Integer needReduce = occupied - stock;
                System.out.println("需要扣除 = " + needReduce);
                List<CartVO> reduceList = cartList.stream()
                        .filter(cart -> "TICKET".equalsIgnoreCase(cart.getProductType()))
                        .filter(cart -> Objects.equals(cart.getProductId(), ticketId))
                        .sorted(
                                Comparator
                                        // 日期由遠到近
                                        .comparing(CartVO::getExpiryDate, Comparator.reverseOrder())
                                        // 同日期票種排序
                                        .thenComparing(cart -> {
                                            switch (cart.getSpec()) {
                                                case "ADULT":
                                                    return 1;
                                                case "CHILD":
                                                    return 2;
                                                case "CONCESSION":
                                                    return 3;
                                                default:
                                                    return 99;
                                            }
                                        })
                        )
                        .toList();
                System.out.println("===== 排序結果 =====");
                for (CartVO cart : reduceList) {
                    System.out.println(
                            cart.getExpiryDate() + " | "
                                    + cart.getSpec() + " | "
                                    + cart.getProductQuantity());
                }

                // 開始扣數量
                for (CartVO cart : reduceList) {
                    if (needReduce <= 0) {
                        break;
                    }
                    Integer quantity = cart.getProductQuantity();
                    if (quantity <= needReduce) {
                        needReduce -= quantity;
                        cart.setProductQuantity(0);
                    } else {
                        cart.setProductQuantity(quantity - needReduce);
                        needReduce = 0;
                    }
                }
                System.out.println("===== 扣除後 =====");
                for (CartVO cart : reduceList) {
                    System.out.println(
                            cart.getExpiryDate() + " | "
                                    + cart.getSpec() + " | "
                                    + cart.getProductQuantity());
                }
            }
            System.out.println("========================");
            } // <-- for(Map.Entry...) 結束
            // ===== 全部 ticketId 都處理完，再移除 =====
            cartList.removeIf(cart -> cart.getProductQuantity() <= 0);
            if (cartChanged) {
                cartService.saveCart(session, cartList);
                throw new RuntimeException("因庫存異動，購物車已自動調整，請再次確認訂單。");
            }
            System.out.println("===== cartList =====");
            for (CartVO cart : cartList) {
                System.out.println(
                        cart.getProductId() + " | "
                                + cart.getSpec() + " | "
                                + cart.getProductQuantity());
            }
    
    }
    
    

}
