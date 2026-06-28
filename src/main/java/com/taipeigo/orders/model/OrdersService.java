package com.taipeigo.orders.model;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class OrdersService {
	
	@Autowired
	OrdersRepository repository;
	
	@Autowired
    private SessionFactory sessionFactory;

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

	public void updateStatus(Integer ordersId, String orderStatus, String paymentStatus) {
		/*如果repository去尋找ordersId，沒有找到東西就拋RuntimeException*/
		OrdersVO orders  = repository.findById(ordersId).orElseThrow(()-> new RuntimeException("找不到訂單"));
		/*如果repository有找到，就把VO回來的東西給你，再把值塞回去對應欄位*/
	    orders.setOrderStatus(orderStatus);
	    orders.setPaymentStatus(paymentStatus);
	    repository.save(orders);

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
