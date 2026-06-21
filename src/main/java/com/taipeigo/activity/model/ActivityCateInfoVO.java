package com.taipeigo.activity.model;

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
@Table(name = "ACTIVITY_CATE_INFO")
public class ActivityCateInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ACTIVITY_CATE_INFO_ID")
    private Integer activityCateInfoId;

    @ManyToOne
    @JoinColumn(name = "ACTIVITY_ID", nullable = false)
    private ActivityVO activity;

    @ManyToOne
    @JoinColumn(name = "ACTIVITY_CATE_ID", nullable = false)
    private ActivityCateVO activityCate;

    public ActivityCateInfoVO() {
    }

    public Integer getActivityCateInfoId() {
        return activityCateInfoId;
    }

    public void setActivityCateInfoId(Integer activityCateInfoId) {
        this.activityCateInfoId = activityCateInfoId;
    }

    public ActivityVO getActivity() {
        return activity;
    }

    public void setActivity(ActivityVO activity) {
        this.activity = activity;
    }

    public ActivityCateVO getActivityCate() {
        return activityCate;
    }

    public void setActivityCate(ActivityCateVO activityCate) {
        this.activityCate = activityCate;
    }
}
