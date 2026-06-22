package com.taipeigo.activity.model;

import java.util.List;

public class ActivitySectionDTO {

    private String categoryName; // 區塊標題名稱
    private List<ActivityVO> activities; // 區塊底下的分類小卡

    public ActivitySectionDTO(String categoryName, List<ActivityVO> activities) {
        this.categoryName = categoryName;
        this.activities = activities;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<ActivityVO> getActivities() {
        return activities;
    }

    public void setActivities(List<ActivityVO> activities) {
        this.activities = activities;
    }

}
