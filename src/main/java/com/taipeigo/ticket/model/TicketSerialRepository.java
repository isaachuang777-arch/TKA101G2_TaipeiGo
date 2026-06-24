package com.taipeigo.ticket.model;

import java.util.Date;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface TicketSerialRepository extends JpaRepository<TicketSerialVO, Integer> {

    // 檢查該筆序號是否已存在在資料庫
    boolean existsBySerialNumber(String serialNumber);

    // 檢查該門票id是否還有足夠庫存 (狀態為 1=可販售 的總張數 是否大於或等於需要的張數)
    // 範例：如果需要 3 張，而庫存有 5 張 ➔ (5 >= 3) ➔ 回傳 true
    @Query("SELECT (COUNT(s) >= :requiredQty) FROM TicketSerialVO s WHERE s.ticketVO.ticketId = :ticketId AND s.status = 1")
    boolean hasEnoughStock(@Param("ticketId") Integer ticketId, @Param("requiredQty") Integer requiredQty);


    // 找出該門票id有的庫存中，最小的門票序號編號物件 (依 ID 升序排序，只取第一筆)
    @Query("SELECT s FROM TicketSerialVO s WHERE s.ticketVO.ticketId = :ticketId AND s.status = :status ORDER BY s.ticketSerialId ASC LIMIT 1")
    Optional<TicketSerialVO> findOldestTicketSerialVO(@Param("ticketId") Integer ticketId, @Param("status") Integer status);

    // 利用門票序號編號 TICKET_SERIAL_ID 更改 1.序號狀態 2.門票使用日期/期限 3.會員編號 4.訂單編號
    // @Modifying  搭配 @Transactional 使用，若失敗自動回復到原本狀態
    @Modifying
    @Transactional
    @Query(value = "UPDATE TICKET_SERIAL SET STATUS = :status, EXPIRY_DATE = :expiryDate, " +
                   "CUST_ID = :custId, ORDER_ID = :orderId WHERE TICKET_SERIAL_ID = :ticketSerialId", 
           nativeQuery = true)
    int updateSerialPurchaseInfo(
		@Param("ticketSerialId") Integer ticketSerialId,
		@Param("status") Integer status,
		@Param("expiryDate") Date expiryDate,
		@Param("custId") Integer custId,
		@Param("orderId") Integer orderId
	);

    // 利用訂單編號 ORDER_ID 去更新 STATUS (整筆訂單取消時，底下的票券集體作廢)
    @Modifying
    @Transactional
    @Query("UPDATE TicketSerialVO s SET s.status = :status WHERE s.ordersVO.orderId = :orderId")
    int updateStatusByOrderId(@Param("orderId") Integer orderId, @Param("status") Integer status);



	// 檢查該門票id是否還有庫存
	// 找出該門票id有的庫存中，找出最小的門票序號編號 TICKET_SERIAL_ID 
	// 利用門票序號編號 TICKET_SERIAL_ID 更改 1.序號狀態 2.門票使用日期/期限 3.會員編號 4.訂單編號 
	// 利用訂單編號 ORDER_ID 去更新 STATUS
	// 檢查門票
}