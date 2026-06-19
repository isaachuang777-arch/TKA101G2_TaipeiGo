package com.taipeigo.orders.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


public interface OrdersRepository   extends JpaRepository<OrdersVO, Integer>{

	
	List<OrdersVO> findByCustId(Integer custId);

}
