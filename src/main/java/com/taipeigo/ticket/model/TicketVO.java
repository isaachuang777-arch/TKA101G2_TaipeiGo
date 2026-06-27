package com.taipeigo.ticket.model;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import com.taipeigo.ticketcategory.model.TicketCategoryVO;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "TICKET")
public class TicketVO implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "TICKET_ID")
	private Integer ticketId;

	@Column(name = "TICKET_NAME", length = 50)
	private String ticketName;

	@Column(name = "TICKET_DESCRIPTION", length = 500)
	private String ticketDescription;

	@Column(name = "TICKET_ADDRESS", length = 50)
	private String ticketAddress;

	@Column(name = "CREATED_AT", updatable = false)
	private Timestamp createdAt;

	@Column(name = "UPDATED_AT")
	private Timestamp updatedAt;

	@Column(name = "TICKET_STATUS")
	private Integer ticketStatus; // 0=未啟用 1=啟用

	@Column(name = "ADULT_ORIGINAL_PRICE")
	private Integer adultOriginalPrice;

	@Column(name = "ADULT_PRICE")
	private Integer adultPrice;

	@Column(name = "CHILD_ORIGINAL_PRICE")
	private Integer childOriginalPrice;

	@Column(name = "CHILD_PRICE")
	private Integer childPrice;

	@Column(name = "CONCESSION_ORIGINAL_PRICE")
	private Integer concessionOriginalPrice;

	@Column(name = "CONCESSION_PRICE")
	private Integer concessionPrice;

	/*
	 * 門票圖片：當門票刪除時，圖片跟著刪除 cascade = CascadeType.ALL
	 * orphanRemoval = true 當圖片刪除，同步在資料庫刪除該筆資料
	 */
	@OneToMany(mappedBy = "ticketVO", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<TicketImageVO> ticketImages = new ArrayList<>();

	// 門票序號
	@OneToMany(mappedBy = "ticketVO")
	@JsonIgnore
	private List<TicketSerialVO> ticketSerials = new ArrayList<>();

	// 門票分類 (透過中介表 TICKET_CATEGORY_INFO 控制)
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "TICKET_CATEGORY_INFO", joinColumns = @JoinColumn(name = "TICKET_ID"), inverseJoinColumns = @JoinColumn(name = "TICKET_CATEGORY_ID"))
	private List<TicketCategoryVO> ticketCategories = new ArrayList<>();

	public TicketVO() {

	}

	public Integer getTicketId() {
		return ticketId;
	}

	public void setTicketId(Integer ticketId) {
		this.ticketId = ticketId;
	}

	public String getTicketName() {
		return ticketName;
	}

	public void setTicketName(String ticketName) {
		this.ticketName = ticketName;
	}

	public String getTicketDescription() {
		return ticketDescription;
	}

	public void setTicketDescription(String ticketDescription) {
		this.ticketDescription = ticketDescription;
	}

	public String getTicketAddress() {
		return ticketAddress;
	}

	public void setTicketAddress(String ticketAddress) {
		this.ticketAddress = ticketAddress;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public Timestamp getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Timestamp updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Integer getTicketStatus() {
		return ticketStatus;
	}

	public void setTicketStatus(Integer ticketStatus) {
		this.ticketStatus = ticketStatus;
	}

	public Integer getAdultOriginalPrice() {
		return adultOriginalPrice;
	}

	public void setAdultOriginalPrice(Integer adultOriginalPrice) {
		this.adultOriginalPrice = adultOriginalPrice;
	}

	public Integer getAdultPrice() {
		return adultPrice;
	}

	public void setAdultPrice(Integer adultPrice) {
		this.adultPrice = adultPrice;
	}

	public Integer getChildOriginalPrice() {
		return childOriginalPrice;
	}

	public void setChildOriginalPrice(Integer childOriginalPrice) {
		this.childOriginalPrice = childOriginalPrice;
	}

	public Integer getChildPrice() {
		return childPrice;
	}

	public void setChildPrice(Integer childPrice) {
		this.childPrice = childPrice;
	}

	public Integer getConcessionOriginalPrice() {
		return concessionOriginalPrice;
	}

	public void setConcessionOriginalPrice(Integer concessionOriginalPrice) {
		this.concessionOriginalPrice = concessionOriginalPrice;
	}

	public Integer getConcessionPrice() {
		return concessionPrice;
	}

	public void setConcessionPrice(Integer concessionPrice) {
		this.concessionPrice = concessionPrice;
	}

	public List<TicketImageVO> getTicketImages() {
		return ticketImages;
	}

	public void setTicketImages(List<TicketImageVO> ticketImages) {
		this.ticketImages = ticketImages;
	}

	public List<TicketSerialVO> getTicketSerials() {
		return ticketSerials;
	}

	public void setTicketSerials(List<TicketSerialVO> ticketSerials) {
		this.ticketSerials = ticketSerials;
	}

	public List<TicketCategoryVO> getTicketCategories() {
		return ticketCategories;
	}

	public void setTicketCategories(List<TicketCategoryVO> ticketCategories) {
		this.ticketCategories = ticketCategories;
	}

	/* 取得還未被購買的序號張數 */
	public long getAvailableSerialCount() {
		if (this.ticketSerials == null) {
			return 0;
		}
		return this.ticketSerials.stream()
				.filter(serial -> serial.getCustomerVO() == null) // 檢查會員物件是不是 null (代表還在)
				.count();
	}

	/* 取得已銷售的序號張數 (狀態為 2=已賣出, 3=已使用, 4=已過期) */
	public long getSoldCount() {
		if (this.ticketSerials == null) {
			return 0;
		}
		long count = 0;
		for (TicketSerialVO serial : this.ticketSerials) {
			Integer status = serial.getStatus();
			if (status != null && (status == 2 || status == 3 || status == 4)) {
				count++;
			}
		}
		return count;
	}

}