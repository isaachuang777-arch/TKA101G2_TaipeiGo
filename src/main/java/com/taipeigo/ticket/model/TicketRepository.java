package com.taipeigo.ticket.model;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<TicketVO, Integer> {

    // 利用已啟用的 categoryId 找到所有取得啟用的門票（ticketStatus = 1）
    @Query("SELECT DISTINCT t FROM TicketVO t " +
           "JOIN t.ticketCategories c " +
           "WHERE c.ticketCategoryId = :categoryId " +
           "AND c.ticketCategoryStatus = 1 " +
           "AND t.ticketStatus = 1")
    Page<TicketVO> findActiveTicketsByCategoryId(@Param("categoryId") Integer categoryId, Pageable pageable);

    // 依門票狀態查詢門票 (0=未啟用(下架) 1=啟用(上架) 2=已刪除)
    Page<TicketVO> findByTicketStatus(Integer ticketStatus, Pageable pageable);
}
