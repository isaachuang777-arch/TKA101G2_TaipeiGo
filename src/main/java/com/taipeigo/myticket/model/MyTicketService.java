package com.taipeigo.myticket.model;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taipeigo.ticket.model.TicketSerialVO;

import jakarta.transaction.Transactional;

@Service
public class MyTicketService {

    @Autowired
    private MyTicketRepository myTicketRepository;

    public List<TicketSerialVO> getMyTickets(Integer custId) {
        return myTicketRepository.findByCustomerVO_CustId(custId);
    }

    public TicketSerialVO getTicketBySerialNumber(String serialNumber) {
        return myTicketRepository.findBySerialNumber(serialNumber).orElse(null);
    }

    @Transactional
    public String verifyAndUseTicket(String serialNumber) {

        TicketSerialVO ticket =
                myTicketRepository.findBySerialNumber(serialNumber).orElse(null);

        if (ticket == null) {
            return "查無此票券";
        }

        if (ticket.getStatus() == 3) {
            return "此票券已使用";
        }

        if (ticket.getStatus() == 4) {
            return "此票券已過期";
        }

        if (ticket.getStatus() == 6) {
            return "此票券已退款";
        }

        if (ticket.getExpiryDate() != null &&
                ticket.getExpiryDate().before(new Timestamp(System.currentTimeMillis()))) {

            ticket.setStatus(4);
            myTicketRepository.save(ticket);

            return "此票券已過期";
        }

        if (ticket.getStatus() != 2) {
            return "此票券目前不可使用";
        }

        ticket.setStatus(3);
        myTicketRepository.save(ticket);

        return "驗票成功";
    }
}