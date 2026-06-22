package com.taipeigo.activity.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActivityCateService {

    private final ActivityCateRepository cateRepository;
    private final ActivityJDBCDAO activityJDBCDAO;

    @Autowired
    public ActivityCateService(ActivityCateRepository cateRepository, ActivityJDBCDAO activityJDBCDAO) {

        this.cateRepository = cateRepository;
        this.activityJDBCDAO = activityJDBCDAO;

    }

}
