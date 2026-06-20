package com.taipeigo.cs.model;

import java.io.Serializable;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.taipeigo.customer.model.CustomerVO;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "CS")

public class CsVO implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // AI
	@Column(name = "CS_ID", updatable = false)
	private Integer csID;

	// ===========CustomerVO.custId 的FK
	@ManyToOne
	@JoinColumn(name = "CUST_ID") // 
	private CustomerVO customerVO; 
	//============
	
	@NotNull(message = "請選擇問題類別")
	@Column(name = "CASE_CATE")
	//11=操作問題, 12=訂單問題, 13=其他
	private Byte caseCate;
	
	@Column(name = "CASE_STATUS")
	//0=新增, 1=待處理, 2=已回覆, 3=已結案
	private Byte caseStatus;
	///////////////////////////////////////
	public static final Byte SsCreated = 0;           
	public static final Byte SsPending  =1;           
	public static final Byte SsReplied = 2;    
	public static final Byte SsResovled = 3;    
	//////////////////////	///////////////// 可以寫成CsVO.setCaseStatus(CsVO.SsPending);
	

	@NotNull
	@CreationTimestamp //Hibernate自動
	@Column(name = "CREATED_AT", updatable = false) //避免更新是動到 所以不是updatable
	private java.sql.Timestamp createdAt;
	
	@UpdateTimestamp //Hibernate自動
	@Column(name = "UPDATED_AT")
	private java.sql.Timestamp updatedAt;
	
	@Column(name = "RESOLVED_AT")
	private java.sql.Timestamp resolvedAt;

	public Integer getCsID() {
		return csID;
	}

	public void setCsID(Integer csID) {
		this.csID = csID;
	}

	public CustomerVO getCustomerVO() {
		return customerVO;
	}

	public void setCustomerVO(CustomerVO customerVO) {
		this.customerVO = customerVO;
	}

	public Byte getCaseCate() {
		return caseCate;
	}

	public void setCaseCate(Byte caseCate) {
		this.caseCate = caseCate;
	}

	public Byte getCaseStatus() {
		return caseStatus;
	}

	public void setCaseStatus(Byte caseStatus) {
		this.caseStatus = caseStatus;
	}

	public java.sql.Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(java.sql.Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public java.sql.Timestamp getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(java.sql.Timestamp updatedAt) {
		this.updatedAt = updatedAt;
	}

	public java.sql.Timestamp getResolvedAt() {
		return resolvedAt;
	}

	public void setResolvedAt(java.sql.Timestamp resolvedAt) {
		this.resolvedAt = resolvedAt;
	}

	// ------------------FK
	@OneToMany(mappedBy = "csVO", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private Set<CsMsgVO> csMsgVOs;
	// -----------------FK

	public Set<CsMsgVO> getCsMsgVOs() {
		return csMsgVOs;
	}

	public void setCsMsgVOs(Set<CsMsgVO> csMsgVOs) {
		this.csMsgVOs = csMsgVOs;
	}

}
