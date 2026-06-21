package com.taipeigo.activity.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "ACTIVITY_IMAGE")
public class ActivityImageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ACTIVITY_IMAGE_ID")
    private Integer activityImageId;

    // 關聯到 ActivityVO (多張圖片對應一個活動)
    @NotNull(message = "所屬一日活動不能為空")
    @ManyToOne
    @JoinColumn(name = "ACTIVITY_ID", nullable = false)
    private ActivityVO activity;

    @Column(name = "ACTIVITY_IMAGE_SRC", length = 250)
    private String activityImageSrc;

    public ActivityImageVO() {
    }

    public Integer getActivityImageId() {
        return activityImageId;
    }

    public void setActivityImageId(Integer activityImageId) {
        this.activityImageId = activityImageId;
    }

    public ActivityVO getActivity() {
        return activity;
    }

    public void setActivity(ActivityVO activity) {
        this.activity = activity;
    }

    public String getActivityImageSrc() {
        return activityImageSrc;
    }

    public void setActivityImageSrc(String activityImageSrc) {
        this.activityImageSrc = activityImageSrc;
    }
}
