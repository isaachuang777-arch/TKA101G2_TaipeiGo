package com.taipeigo.ticket.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketSerialRepository extends JpaRepository<TicketSerialVO, Integer> {

	
	// 檢查該筆序號是否已存在在資料庫
	boolean existsBySerialNumber(String serialNumber);
}
