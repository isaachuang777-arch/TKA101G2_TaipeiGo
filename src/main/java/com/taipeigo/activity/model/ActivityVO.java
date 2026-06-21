package com.taipeigo.activity.model;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "ACTIVITY")
public class ActivityVO implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ACTIVITY_ID")
	private Integer activityId;

	@NotBlank(message = "一日活動名稱必填")
	@Size(max = 150, message = "活動名稱150個字為上限")
	@Column(name = "ACTIVITY_NAME", nullable = false, length = 150)
	private String activityName;

	@Column(name = "ACTIVITY_DESC", columnDefinition = "LONGTEXT")
	private String activityDesc;

	@NotNull(message = "有效天數必填")
	@Min(value = 1, message = "有效期限不能小於或等於0")
	@Column(name = "EXPIRY_DATE", nullable = false)
	private Integer expiryDate;

	@NotNull(message = "折扣欄必填")
	@Min(value = 0, message = "折扣不得為負數")
	@Column(name = "DISCOUNT", nullable = false)
	private Integer discount;

	@NotNull(message = "一日活動狀態不能為空")
	@Column(name = "ACTIVITY_STATUS", nullable = false)
	private Integer activityStatus;

	@OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ActivityDetailVO> activityDetails;

	@OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ActivityImageVO> activityImage;

	public ActivityVO() {
	}

	public Integer getActivityId() {
		return activityId;
	}

	public void setActivityId(Integer activityId) {
		this.activityId = activityId;
	}

	public String getActivityName() {
		return activityName;
	}

	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

	public String getActivityDesc() {
		return activityDesc;
	}

	public void setActivityDesc(String activityDesc) {
		this.activityDesc = activityDesc;
	}

	public Integer getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Integer expiryDate) {
		this.expiryDate = expiryDate;
	}

	public Integer getDiscount() {
		return discount;
	}

	public void setDiscount(Integer discount) {
		this.discount = discount;
	}

	public Integer getActivityStatus() {
		return activityStatus;
	}

	public void setActivityStatus(Integer activityStatus) {
		this.activityStatus = activityStatus;
	}

	public List<ActivityDetailVO> getActivityDetails() {
		return activityDetails;
	}

	public void setActivityDetails(List<ActivityDetailVO> activityDetails) {
		this.activityDetails = activityDetails;
	}

	public List<ActivityImageVO> getActivityImage() {
		return activityImage;
	}

	public void setActivityImage(List<ActivityImageVO> activityImage) {
		this.activityImage = activityImage;
	}

}
