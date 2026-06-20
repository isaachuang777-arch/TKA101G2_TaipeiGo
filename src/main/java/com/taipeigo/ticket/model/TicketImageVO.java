package com.taipeigo.ticket.model;

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
@Table(name = "TICKET_IMAGE")
public class TicketImageVO implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "TICKET_IMAGE_ID")
	private Integer ticketImageId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TICKET_ID")
	private TicketVO ticketVO;

	@Column(name = "TICKET_IMAGE_SRC", length = 250)
	private String ticketImageSrc;
	
	public TicketImageVO() {
	}

	public Integer getTicketImageId() {
		return ticketImageId;
	}

	public void setTicketImageId(Integer ticketImageId) {
		this.ticketImageId = ticketImageId;
	}

	public TicketVO getTicketVO() {
		return ticketVO;
	}

	public void setTicketVO(TicketVO ticketVO) {
		this.ticketVO = ticketVO;
	}

	public String getTicketImageSrc() {
		return ticketImageSrc;
	}

	public void setTicketImageSrc(String ticketImageSrc) {
		this.ticketImageSrc = ticketImageSrc;
	}
	

}
