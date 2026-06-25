package com.taipeigo.cart.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository {

	void addItem(Integer custId, CartVO cartVO);

	List<CartVO> getCart(Integer custId);

	void removeItem(Integer custId, Integer productId);

	void clearCart(Integer custId);
	
	
}
