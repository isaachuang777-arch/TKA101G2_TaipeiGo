package com.taipeigo.favorite.model;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taipeigo.product.model.ProductVO;

public interface FavoriteProductRepository extends JpaRepository<ProductVO, Integer> {

    // 依活動編號查詢商品
    Optional<ProductVO> findByActivityId(Integer activityId);

    // 依門票編號查詢商品
    Optional<ProductVO> findByTicketId(Integer ticketId);

}