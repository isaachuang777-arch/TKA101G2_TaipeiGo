package com.taipeigo.admin.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ADM_PER")
public class AdmPerVO implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "PER_ID", updatable = false)
	private Integer perId;

	@ManyToOne
	@JoinColumn(name = "ADM_ID") // FK ->ADMIN 的ADM_ID
	private AdminVO adminVO; // 在Java他生在AdminVO

	@ManyToOne
	@JoinColumn(name = "FUNC_ID") // FK ->ADM_FUNCTION的FUNC_ID
	private AdmFuncVO admfuncVO;

	public Integer getPerId() {
		return perId;
	}

	public void setPerId(Integer perId) {
		this.perId = perId;
	}

	public AdminVO getAdminVO() {
		return adminVO;
	}

	public void setAdminVO(AdminVO adminVO) {
		this.adminVO = adminVO;
	}

	public AdmFuncVO getAdmfuncVO() {
		return admfuncVO;
	}

	public void setAdmfuncVO(AdmFuncVO admfuncVO) {
		this.admfuncVO = admfuncVO;
	}

	@Override
	public String toString() {
		return "AdmPerVO [perId=" + perId + ", adminVO=" + adminVO + ", admfuncVO=" + admfuncVO + "]";
	}
}
