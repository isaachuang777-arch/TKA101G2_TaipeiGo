package com.taipeigo.orders.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


public interface OrdersRepository   extends JpaRepository<OrdersVO, Integer>{

	
	List<OrdersVO> findByCustId(Integer custId);

	@Modifying
	@Transactional
	@Query(value = """
	    UPDATE ORDERS
	    SET  ORDER_STATUS = :orderStatus, PAYMENT_STATUS = :paymentStatus	   
	    WHERE ORDER_ID = :ordersId""", 
	    nativeQuery = true)
	void updateStatus(
	        @Param("ordersId") Integer ordersId,
	        @Param("orderStatus") String orderStatus,
	        @Param("paymentStatus") String paymentStatus);
}
