package com.taipeigo.orders.model;

import java.util.List;

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



	
	
	
}
