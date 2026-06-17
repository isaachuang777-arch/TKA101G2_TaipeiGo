package com.taipeigo.ticketcategory.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="TICKET_CATEGORY")
public class TicketCategoryVO implements java.io.Serializable{
	private static final long serialVersionUID = 1L;
	
	private Integer ticketCategoryId;
	private String ticketCategoryName;
	private Integer ticketCategoryStatus;
	
	public TicketCategoryVO() {}
	
	public TicketCategoryVO(String ticketCategoryName, Integer ticketCategoryStatus) {
		this.ticketCategoryName = ticketCategoryName;
		this.ticketCategoryStatus = ticketCategoryStatus;
	}

	
	@Id
	@Column(name="TICKET_CATEGORY_ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer getTicketCategoryId() {
		return ticketCategoryId;
	}

	public void setTicketCategoryId(Integer ticketCategoryId) {
		this.ticketCategoryId = ticketCategoryId;
	}
	
	@Column(name="TICKET_CATEGORY_NAME")
	public String getTicketCategoryName() {
		return ticketCategoryName;
	}

	public void setTicketCategoryName(String ticketCategoryName) {
		this.ticketCategoryName = ticketCategoryName;
	}
	
	@Column(name="TICKET_CATEGORY_STATUS")
	@JsonIgnore
	public Integer getTicketCategoryStatus() {
		return ticketCategoryStatus;
	}

	public void setTicketCategoryStatus(Integer ticketCategoryStatus) {
		this.ticketCategoryStatus = ticketCategoryStatus;
	}
	
}

