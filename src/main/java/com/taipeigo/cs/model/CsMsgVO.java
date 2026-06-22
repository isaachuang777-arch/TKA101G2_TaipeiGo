package com.taipeigo.cs.model;

import java.io.Serializable;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.Length;

import com.taipeigo.admin.model.AdminVO;
import com.taipeigo.customer.model.CustomerVO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "CS_MSG")
public class CsMsgVO implements Serializable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // AI
	@Column(name = "MSG_ID", updatable = false)
	private Integer msgID;
	
	// ===========CsVO.csId 的FK
	@ManyToOne
	@JoinColumn(name = "CS_ID") // 
	private CsVO csVO; 
	//============

	// ===========CustomerVO.custId 的FK
	@ManyToOne
	@JoinColumn(name = "CUST_ID") 
	private CustomerVO customerVO; 
	//============
	
	// ===========AdminVO.admId 的FK
	@ManyToOne
	@JoinColumn(name = "ADM_ID") 
	private AdminVO adminVO; 
	//============
	
	@Column(name = "SENDER_TYPE")
	private Byte senderType;
	//----------------------------------------------
	public static final Byte Srcust = 0;
	public static final Byte Sradmin = 1;
	public static final Byte Srsystem = 3;
	public static final Byte Srworknote = 7;
	//---------------------------------------------- 可以寫成CsMMsgVO.setSenderType(CsMsgVO.SsPending);
	
	
	@NotNull(message = "訊息內容不可為空")
    @Length(min = 1, max = 500, message = "訊息內容500字以內")
	@Column(name= "MSG_CONTENT")
    private String msgContent;
    
    @Length(min = 1, max = 200)
    @Column(name = "MSG_IMGSRC")
    private String msgImgsrc;
    

	@CreationTimestamp //Hibernate自動
    @Column(name = "CREATED_AT")
    private java.sql.Timestamp createdAt;

	public Integer getMsgID() {
		return msgID;
	}

	public void setMsgID(Integer msgID) {
		this.msgID = msgID;
	}

	public CsVO getCsVO() {
		return csVO;
	}

	public void setCsVO(CsVO csVO) {
		this.csVO = csVO;
	}

	public CustomerVO getCustomerVO() {
		return customerVO;
	}

	public void setCustomerVO(CustomerVO customerVO) {
		this.customerVO = customerVO;
	}

	public AdminVO getAdminVO() {
		return adminVO;
	}

	public void setAdminVO(AdminVO adminVO) {
		this.adminVO = adminVO;
	}

	public Byte getSenderType() {
		return senderType;
	}

	public void setSenderType(Byte senderType) {
		this.senderType = senderType;
	}

	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	public String getMsgImgsrc() {
		return msgImgsrc;
	}

	public void setMsgImgsrc(String msgImgsrc) {
		this.msgImgsrc = msgImgsrc;
	}

	public java.sql.Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(java.sql.Timestamp createdAt) {
		this.createdAt = createdAt;
	}
    
    
    
}
