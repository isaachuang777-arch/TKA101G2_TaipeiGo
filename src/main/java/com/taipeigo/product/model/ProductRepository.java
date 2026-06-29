package com.taipeigo.product.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.lettuce.core.dynamic.annotation.Param;


@Repository
public interface ProductRepository extends JpaRepository<ProductVO, Integer>{

    @Query("SELECT p FROM ProductVO p WHERE p.productName LIKE %:keyword%")
    Page<ProductVO> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

}
