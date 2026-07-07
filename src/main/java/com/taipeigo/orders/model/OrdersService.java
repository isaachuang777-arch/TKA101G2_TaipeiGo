package com.taipeigo.orders.model;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.taipeigo.ticket.model.TicketSerialRepository;
import com.taipeigo.ticket.model.TicketSerialVO;
import com.taipeigo.ticket.model.TicketService;


@Service
public class OrdersService {
	
	@Autowired
	OrdersRepository repository;
	
	@Autowired
	private TicketService ticketService;
	
	@Autowired
	private TicketSerialRepository ticketSerialRepository;

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
		/***取消之前先判斷票券是否已經被使用，或是已過期***/
		if("取消".equals(orderStatus)) {
			List<TicketSerialVO> serialList = repository.findTicketSerialByOrderId(ordersId);
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	        
	        for(TicketSerialVO serial : serialList) {
	        	/**已使用 TicketSerialVO中status是3時，代表票券狀態是"已被使用"(**/
	        	if(serial.getStatus() == 3 ) {
	                throw new RuntimeException("此訂單已有票券完成使用，無法取消。");
	        	}
	        	/**已過期 TicketSerialVO中status是4時，代表票券狀態是"已過期"(**/
	        	if(serial.getStatus() == 4) {
	        		String expiryDate = serial.getExpiryDate()
	        				 .toLocalDateTime()
	                         .toLocalDate()
	                         .format(formatter);
	        		throw new RuntimeException(  "此訂單票券已於 " + expiryDate + " 過期，無法取消。");
	        	}
	        }
		}		
		
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

/**取消Message，供畫面顯示提示訊息***/
	public String getCancelMessage(Integer ordersId) {
	    List<TicketSerialVO> serialList = repository.findTicketSerialByOrderId(ordersId);
	    DateTimeFormatter formatter =DateTimeFormatter.ofPattern("yyyy-MM-dd");
	    for (TicketSerialVO serial : serialList) {
	        if (serial.getStatus() == 3) {
	            return "此訂單已有票券完成使用，無法取消。";
	        }
	        if (serial.getStatus() == 4) {
	            String expiryDate = serial.getExpiryDate()
	                    .toLocalDateTime()
	                    .toLocalDate()
	                    .format(formatter);

	            return "此訂單票券已於 "
	                    + expiryDate
	                    + " 過期，無法取消。";
	        }
	    }

	    return null;
	}

	
}
