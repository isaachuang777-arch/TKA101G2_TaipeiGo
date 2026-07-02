package com.taipeigo.cart.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**指定JSON傳出時，順序先 ticketId 再 quantity**/
@JsonPropertyOrder({"ticketId", "quantity"})
public class TicketStockDTO {	

	/**傳給ticket確認庫存**/
	private Integer ticketId;
	private Integer quantity;
	
	public Integer getTicketId() {
		return ticketId;
	}
	public void setTicketId(Integer ticketId) {
		this.ticketId = ticketId;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	
}
