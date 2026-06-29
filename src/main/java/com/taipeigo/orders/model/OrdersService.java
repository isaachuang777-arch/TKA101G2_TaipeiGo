package com.taipeigo.orders.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.taipeigo.ticket.model.TicketService;


@Service
public class OrdersService {
	
	@Autowired
	OrdersRepository repository;
	
	@Autowired
	private TicketService ticketService;

	public List<OrdersVO> getAll() {
		return repository.findAll();
	}
	
	public OrdersVO getOrdersId(Integer ordersId) {
		Optional<OrdersVO> optional=repository.findById(ordersId);
		/*如果集合optional查詢回來有值就給值，沒有就給()裡的東西*/
		return optional.orElse(null);
		
	}


	public List<OrdersVO> getByCustId(Integer custId) {
		return repository.findByCustId(custId);

	}

	public void updateStatus(Integer ordersId, String orderStatus) {
		/**如果訂單狀態(orderStatus)取消，付款狀態(paymentStatus)改成"退款"*/
		String paymentStatus = "已付款";
		
		if("取消".equals(orderStatus)) {
			paymentStatus = "退款";
		}
		
		repository.updateStatus(ordersId, orderStatus, paymentStatus);
		/*防呆機制！不會為改變不小心觸發更新而改錯**/
		if("取消".equals(orderStatus)) {
			ticketService.cancelTicketSerial(ordersId);
		}
	}

	public Page<OrdersVO> findAll(Pageable pageable) {
		 return repository.findAll(pageable);
	}

/**結帳-新增訂單***/
	public OrdersVO createOrder(Integer custId, Integer orderTotal) {
	    OrdersVO order = new OrdersVO();
	    order.setCustId(custId);

	    /* 付款成功即建立訂單 */
	    order.setOrderStatus("已完成");
	    order.setPaymentStatus("已付款");
	    order.setPaymentMethod("刷卡");
	    order.setOrderTotal(orderTotal);
	    order.setCreatedAt(new java.sql.Date(System.currentTimeMillis()));

	    return repository.save(order);
	}

	
	
	
}
