package com.taipeigo.orders.model;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;


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



	
	
	
}
