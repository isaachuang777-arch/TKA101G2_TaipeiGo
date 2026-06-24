package com.taipeigo.myticket.model;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taipeigo.ticket.model.TicketSerialVO;

@Service
public class MyTicketService {

    @Autowired
    private MyTicketRepository myTicketRepository;

    public List<TicketSerialVO> getMyTickets(Integer custId) {
        return myTicketRepository.findByCustomerVO_CustId(custId);
    }
}