package com.taipeigo.activity.model;

import java.io.Serializable;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "ACTIVITY_CATE")
public class ActivityCateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ACTIVITY_CATE_ID")
    private Integer activityCateId;

    @NotEmpty(message = "分類名稱不為空值")
    @Size(max = 150, message = "分類名稱150個字為上限")
    @Column(name = "CATE_NAME", nullable = false, length = 150)
    private String cateName;

    @jakarta.validation.constraints.NotNull(message = "啟用狀態不能為空")
    @Column(name = "IS_ACTIVE", nullable = false)
    private Integer isActive;

    @Column(name = "CATE_ICON", columnDefinition = "LONGBLOB")
    private byte[] cateIcon;

    @JsonIgnore
    @OneToMany(mappedBy = "activityCate", cascade = CascadeType.ALL)
    private Set<ActivityCateInfoVO> activityCateInfos;

    public ActivityCateVO() {
    }

    public ActivityCateVO(Integer activityCateId, String cateName, Integer isActive, byte[] cateIcon) {
        this.activityCateId = activityCateId;
        this.cateName = cateName;
        this.isActive = isActive;
        this.cateIcon = cateIcon;
    }

    public Integer getActivityCateId() {
        return activityCateId;
    }

    public void setActivityCateId(Integer activityCateId) {
        this.activityCateId = activityCateId;
    }

    public String getCateName() {
        return cateName;
    }

    public void setCateName(String cateName) {
        this.cateName = cateName;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public byte[] getCateIcon() {
        return cateIcon;
    }

    public void setCateIcon(byte[] cateIcon) {
        this.cateIcon = cateIcon;
    }

    public Set<ActivityCateInfoVO> getActivityCateInfos() {
        return activityCateInfos;
    }

    public void setActivityCateInfos(Set<ActivityCateInfoVO> activityCateInfos) {
        this.activityCateInfos = activityCateInfos;
    }

}
