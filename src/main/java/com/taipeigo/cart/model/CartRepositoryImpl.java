package com.taipeigo.cart.model;

import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public class CartRepositoryImpl implements CartRepository {
	@Override
	public void addItem(Integer custId, CartVO cartVO) {

	}

	@Override
	public List<CartVO> getCart(Integer custId) {
		return null;
	}

	@Override
	public void removeItem(Integer custId, Integer productId) {

	}

	@Override
	public void clearCart(Integer custId) {

	}
	
	
}
