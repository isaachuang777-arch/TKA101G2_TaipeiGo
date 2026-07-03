package com.taipeigo.activity.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<ActivityVO, Integer> {

    long countByActivityStatus(Integer status);
    
    List<ActivityVO> findByIsRecommended(Integer isRecommended);
}