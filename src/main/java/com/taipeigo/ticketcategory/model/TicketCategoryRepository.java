package com.taipeigo.ticketcategory.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TicketCategoryRepository extends JpaRepository<TicketCategoryVO, Integer> {
	
	// （自訂）條件查詢：只撈出特定啟用狀態的門票分類
	@Query(value = "from TicketCategoryVO where ticketCategoryStatus = ?1")
    List<TicketCategoryVO> findByTicketCategoryStatus(Integer ticketCategoryStatus);

}

