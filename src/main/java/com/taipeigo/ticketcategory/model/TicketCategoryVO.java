package com.taipeigo.ticketcategory.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name="TICKET_CATEGORY")
public class TicketCategoryVO implements java.io.Serializable{
	private static final long serialVersionUID = 1L;
	
	private Integer ticketCategoryId;
	
	@NotBlank(message = "門票分類名稱不能留白")
	private String ticketCategoryName;

	@NotNull(message = "請選擇啟用狀態")
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

