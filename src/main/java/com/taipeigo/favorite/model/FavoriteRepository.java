package com.taipeigo.favorite.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<FavoriteVO, Integer> {

    // 查詢某位會員的所有我的最愛
    List<FavoriteVO> findByCustId(Integer custId);

    // 判斷是否已加入我的最愛
    boolean existsByCustIdAndProductId(Integer custId, Integer productId);

    // 查詢某一筆會員的我的最愛
    Optional<FavoriteVO> findByCustIdAndProductId(Integer custId, Integer productId);

    // 刪除指定會員的我的最愛
    void deleteByCustIdAndProductId(Integer custId, Integer productId);
    

}