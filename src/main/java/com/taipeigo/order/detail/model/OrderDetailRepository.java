package com.taipeigo.order.detail.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDetailRepository  extends JpaRepository<OrderDetailVO, Integer>{

	List<OrderDetailVO> findByOrdersVO_OrderId(Integer orderId);

}
