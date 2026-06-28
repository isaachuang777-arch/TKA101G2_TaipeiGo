package com.taipeigo.faq.model;

import java.io.Serializable;

import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.Length;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "FAQ")
public class FaqVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // AI
	@Column(name = "FAQ_ID", updatable = false)
	private Integer faqId;
	
	@NotNull(message = "請輸入主題")
	@Length(max = 40, message = "主題40字以內")
	@Column(name = "TITLE")
	private String title;
	
	@NotNull(message = "請選擇FAQ分類")
	@Column(name = "CATEGORY")
	private Byte category;
	
	@NotNull(message = "請輸入內容")
	@Length(min = 1, max = 1000, message = "內容1000字以內")
	@Column(name = "CONTENT")
	private String content;
	
	@NotNull(message = "請選擇狀態")
	@Column(name = "STATUS")
	//0=隱藏, 1=顯示
	private Byte status;
	
	@UpdateTimestamp
	@Column(name = "CREATE_TIME")
	private java.sql.Timestamp createTime;

	public Byte getCategory() {
		return category;
	}

	public void setCategory(Byte category) {
		this.category = category;
	}

	public Integer getFaqId() {
		return faqId;
	}

	public void setFaqId(Integer faqId) {
		this.faqId = faqId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Byte getStatus() {
		return status;
	}

	public void setStatus(Byte status) {
		this.status = status;
	}

	public java.sql.Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(java.sql.Timestamp createTime) {
		this.createTime = createTime;
	}
	
	
}
