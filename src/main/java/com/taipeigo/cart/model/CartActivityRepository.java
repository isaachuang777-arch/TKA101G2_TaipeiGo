package com.taipeigo.cart.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taipeigo.activity.model.ActivityDetailVO;

public interface CartActivityRepository extends JpaRepository<ActivityDetailVO, Integer>{
    List<ActivityDetailVO> findByActivity_ActivityId(Integer activityId);

}
