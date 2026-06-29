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

       // 因為 JpaRepository 不支援 limit，所以改用原生 SQL 限制
       // 依熱門程度查詢已上架門票 (依狀態為2（已售出）、3（已使用）、4（已過期）的序號數量降序排列，並用 limit 限制筆數)
       @Query(value = "SELECT t.* FROM TICKET t " +
                     "LEFT JOIN TICKET_SERIAL s ON t.TICKET_ID = s.TICKET_ID " +
                     "WHERE t.TICKET_STATUS = 1 " +
                     "GROUP BY t.TICKET_ID " +
                     "ORDER BY SUM(CASE WHEN s.STATUS IN (2, 3, 4) THEN 1 ELSE 0 END) DESC, t.TICKET_ID ASC " +
                     "LIMIT :limit", nativeQuery = true)
       List<TicketVO> findPopularTickets(@Param("limit") int limit);

       // 撈出所有門票 （分頁）
       @Query(value = "from TicketVO order by ticketId asc")
       Page<TicketVO> findAllTickets(Pageable pageable);

       // 模糊搜尋門票 (搜尋門票名稱或分類名稱) （分頁）（不會顯示已被刪除門票分類的結果）
       @Query("SELECT DISTINCT t FROM TicketVO t " +
                     "LEFT JOIN t.ticketCategories c " +
                     "WHERE t.ticketName LIKE concat('%', :keyword, '%') " +
                     "OR (c.ticketCategoryName LIKE concat('%', :keyword, '%') AND c.ticketCategoryStatus != 2) " +
                     "ORDER BY t.ticketId ASC")
       Page<TicketVO> searchTickets(@Param("keyword") String keyword, Pageable pageable);
}
