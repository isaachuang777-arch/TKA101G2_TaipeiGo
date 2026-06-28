package com.taipeigo.ticketcategory.model;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface TicketCategoryRepository extends JpaRepository<TicketCategoryVO, Integer> {

	// （自訂）條件查詢：只撈出啟用(ticketCategoryStatus = 1)狀態的門票分類
	@Query(value = "from TicketCategoryVO where ticketCategoryStatus = ?1")
	List<TicketCategoryVO> findByTicketCategoryStatus(Integer ticketCategoryStatus);

	// 撈出所有未被刪除的分類 (ticketCategoryStatus !=2 )
	@Query(value = "from TicketCategoryVO where ticketCategoryStatus != 2 order by ticketCategoryId asc")
	List<TicketCategoryVO> findAllNotDeleted();

	// 撈出所有未被刪除的分類 (ticketCategoryStatus != 2) [分頁]
	@Query(value = "from TicketCategoryVO where ticketCategoryStatus != 2 order by ticketCategoryId asc")
	Page<TicketCategoryVO> findAllNotDeleted(Pageable pageable);

	// 模糊搜尋未被刪除的分類 [分頁]
	@Query(value = "from TicketCategoryVO where ticketCategoryStatus != 2 and ticketCategoryName like concat('%', ?1, '%') order by ticketCategoryId asc")
	Page<TicketCategoryVO> findByName(String keyword, Pageable pageable);

	// 刪除 TICKET_CATEGORY_INFO 中關聯到該分類的資料
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM TICKET_CATEGORY_INFO WHERE TICKET_CATEGORY_ID = ?1", nativeQuery = true)
	void deleteAssociationsByCategoryId(Integer ticketCategoryId);

}
