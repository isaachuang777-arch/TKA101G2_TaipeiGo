package com.taipeigo.order.detail.model;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailService {
	
	
    @Autowired
    private OrderDetailRepository repository;

	public List<OrderDetailVO> findByOrderId(Integer orderId) {
		return repository.findByOrdersVO_OrderId(orderId);
	}

}
