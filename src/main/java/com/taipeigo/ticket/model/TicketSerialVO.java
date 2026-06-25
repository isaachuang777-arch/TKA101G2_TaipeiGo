package com.taipeigo.ticket.model;

import java.sql.Timestamp;

import com.taipeigo.customer.model.CustomerVO;
import com.taipeigo.orders.model.OrdersVO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "TICKET_SERIAL")
public class TicketSerialVO {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "TICKET_SERIAL_ID")
	private Integer ticketSerialId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TICKET_ID")
	private TicketVO ticketVO;

	// TODO: 測試後再補上唯一性
	@Column(name = "SERIAL_NUMBER", length = 250)
	private String serialNumber;

	@Column(name = "STATUS")
	private Integer status;

	@Column(name = "EXPIRY_DATE")
	private Timestamp expiryDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CUST_ID")
	private CustomerVO customerVO;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ORDER_ID")
	private OrdersVO ordersVO;

	public Integer getTicketSerialId() {
		return ticketSerialId;
	}

	public void setTicketSerialId(Integer ticketSerialId) {
		this.ticketSerialId = ticketSerialId;
	}

	public TicketVO getTicketVO() {
		return ticketVO;
	}

	public void setTicketVO(TicketVO ticketVO) {
		this.ticketVO = ticketVO;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Timestamp getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Timestamp expiryDate) {
		this.expiryDate = expiryDate;
	}

	public CustomerVO getCustomerVO() {
		return customerVO;
	}

	public void setCustomerVO(CustomerVO customerVO) {
		this.customerVO = customerVO;
	}

	public OrdersVO getOrdersVO() {
		return ordersVO;
	}

	public void setOrdersVO(OrdersVO ordersVO) {
		this.ordersVO = ordersVO;
	}

}