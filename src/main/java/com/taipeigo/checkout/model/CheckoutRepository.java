package com.taipeigo.checkout.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.taipeigo.ticket.model.TicketSerialVO;

public interface CheckoutRepository extends JpaRepository<TicketSerialVO, Integer> {

    @Query("""
        SELECT COUNT(s)
        FROM TicketSerialVO s
        WHERE s.ticketVO.ticketId = :ticketId
        AND s.status = 1
    """)
    Integer getAvailableStock(@Param("ticketId") Integer ticketId);

}