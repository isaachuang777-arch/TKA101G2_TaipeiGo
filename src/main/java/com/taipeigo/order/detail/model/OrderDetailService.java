package com.taipeigo.order.detail.model;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taipeigo.orders.model.OrdersVO;
import com.taipeigo.product.dto.CartItemDTO;

@Service
public class OrderDetailService {
	
	
    @Autowired
    private OrderDetailRepository repository;

	public List<OrderDetailVO> findByOrderId(Integer orderId) {
		return repository.findByOrdersVO_OrderId(orderId);
	}

	
/*===============createOrderDetail======建立訂單明細====================================================*/
	public void createOrderDetail(OrdersVO order, CartItemDTO item) {
	    OrderDetailVO detail = new OrderDetailVO();

	    /**訂單ordersVO進來的東西傳給detailVO***/
	    detail.setOrdersVO(order);
	    /**** 商品資訊****/
	    detail.setProductId(item.getProductId());
	    detail.setProductQuantity(item.getQuantity());
	    detail.setOrderDetailSubtotal(item.getSubtotal());
	    detail.setOrderDetailRemark(item.getProductName());
	    repository.save(detail);
	}
}
