package com.taipeigo.order.detail.model;



import com.taipeigo.orders.model.OrdersVO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="order_detail")
public class OrderDetailVO implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ORDER_DETAIL_ID", updatable = false)
	private Integer orderDetailId;

	@Column(name = "PRODUCT_ID")
	private Integer productId;

	@Column(name = "PRODUCT_QUANTITY")
	private Integer productQuantity;

	@Column(name = "ORDER_DETAIL_SUBTOTAL")
	private Integer orderDetailSubtotal;

	@Column(name = "ORDER_DETAIL_REMARK")
	private String orderDetailRemark;

	@ManyToOne
	@JoinColumn(name = "ORDER_ID")
	private OrdersVO ordersVO;

	public OrderDetailVO() {
	}

	public Integer getOrderDetailId() {
		return orderDetailId;
	}

	public void setOrderDetailId(Integer orderDetailId) {
		this.orderDetailId = orderDetailId;
	}

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public Integer getProductQuantity() {
		return productQuantity;
	}

	public void setProductQuantity(Integer productQuantity) {
		this.productQuantity = productQuantity;
	}

	public Integer getOrderDetailSubtotal() {
		return orderDetailSubtotal;
	}

	public void setOrderDetailSubtotal(Integer orderDetailSubtotal) {
		this.orderDetailSubtotal = orderDetailSubtotal;
	}

	public String getOrderDetailRemark() {
		return orderDetailRemark;
	}

	public void setOrderDetailRemark(String orderDetailRemark) {
		this.orderDetailRemark = orderDetailRemark;
	}

}
