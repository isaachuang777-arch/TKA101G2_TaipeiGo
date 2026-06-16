package com.taipeigo.orders.model;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

import com.taipeigo.order.detail.model.OrderDetailVO;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class OrdersVO implements  java.io.Serializable{
	private static final long serialVersionUID=1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ORDER_ID")
	private Integer orderId;

	@Column(name = "CUST_ID")
	private Integer custId;

	@Column(name = "ORDER_STATUS")
	private String orderStatus;

	@Column(name = "PAYMENT_STATUS")
	private String paymentStatus;

	@Column(name = "PAYMENT_METHOD")
	private String paymentMethod;

	@Column(name = "ORDER_TOTAL")
	private Integer orderTotal;
	
	@Column(name="CREATED_AT")
	private Date createdAt;
	
	@OneToMany(
			cascade=CascadeType.ALL, 
			fetch=FetchType.EAGER, 
			mappedBy="ordersVO"
			)
	@OrderBy("orderDetailId asc")
	private Set<OrderDetailVO> orderDetails = new HashSet<OrderDetailVO>();

	public OrdersVO() {
		super();
	}

	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	public Integer getCustId() {
		return custId;
	}

	public void setCustId(Integer custId) {
		this.custId = custId;
	}

	public String getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public Integer getOrderTotal() {
		return orderTotal;
	}

	public void setOrderTotal(Integer orderTotal) {
		this.orderTotal = orderTotal;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Set<OrderDetailVO> getOrderDetails() {
		return orderDetails;
	}

	public void setOrderDetails(Set<OrderDetailVO> orderDetails) {
		this.orderDetails = orderDetails;
	}

	


	
}
