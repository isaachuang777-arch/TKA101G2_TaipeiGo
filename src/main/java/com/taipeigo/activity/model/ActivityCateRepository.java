package com.taipeigo.activity.model;

import org.springframework.data.jpa.repository.Query;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityCateRepository extends JpaRepository<ActivityCateVO, Integer> {

    @Query("FROM ActivityCateVO WHERE isActive = 1")
    List<ActivityCateVO> findAllActiveCategories();

}
