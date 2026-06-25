package com.taipeigo.myticket.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taipeigo.ticket.model.TicketSerialVO;

public interface MyTicketRepository extends JpaRepository<TicketSerialVO, Integer> {

    List<TicketSerialVO> findByCustomerVO_CustId(Integer custId);

    Optional<TicketSerialVO> findBySerialNumber(String serialNumber);
}