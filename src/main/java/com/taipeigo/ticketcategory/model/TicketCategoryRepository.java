package com.taipeigo.ticketcategory.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TicketCategoryRepository extends JpaRepository<TicketCategoryVO, Integer> {

	// （自訂）條件查詢：只撈出啟用(ticketCategoryStatus = 1)狀態的門票分類
	@Query(value = "from TicketCategoryVO where ticketCategoryStatus = ?1")
	List<TicketCategoryVO> findByTicketCategoryStatus(Integer ticketCategoryStatus);

	// 撈出所有未被刪除的分類 (ticketCategoryStatus !=2 )
	@Query(value = "from TicketCategoryVO where ticketCategoryStatus != 2 order by ticketCategoryId asc")
	List<TicketCategoryVO> findAllNotDeleted();

}
