package com.taipeigo.myticket.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taipeigo.ticket.model.TicketSerialVO;

import jakarta.transaction.Transactional;

@Service
public class MyTicketService {

    @Autowired
    private MyTicketRepository myTicketRepository;

    @Transactional
    public List<TicketSerialVO> getMyTickets(Integer custId) {

        List<TicketSerialVO> tickets =
                myTicketRepository.findByCustomerVO_CustId(custId);

        LocalDateTime now = LocalDateTime.now();

        // 進入我的票券時，先把已過期但還是未使用的票券改成已過期
        for (TicketSerialVO ticket : tickets) {

            if (ticket.getStatus() == 2 && ticket.getExpiryDate() != null) {

                LocalDateTime expiryDate =
                        ticket.getExpiryDate().toLocalDateTime();

                if (now.isAfter(expiryDate)) {
                    ticket.setStatus(4);
                    myTicketRepository.save(ticket);
                }
            }
        }

        // 已退款不顯示
        tickets.removeIf(ticket -> ticket.getStatus() == 6);

        // 排序：未使用 > 已使用 > 已過期，日期近的在前
        tickets.sort(
                Comparator
                        .comparingInt(this::getStatusRank)
                        .thenComparing(
                                TicketSerialVO::getExpiryDate,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
        );

        return tickets;
    }

    private int getStatusRank(TicketSerialVO ticket) {

        if (ticket.getStatus() == 2) {
            return 1; // 未使用
        }

        if (ticket.getStatus() == 3) {
            return 2; // 已使用
        }

        if (ticket.getStatus() == 4) {
            return 3; // 已過期
        }

        return 99;
    }

    public TicketSerialVO getTicketBySerialNumber(String serialNumber) {
        return myTicketRepository.findBySerialNumber(serialNumber).orElse(null);
    }

    public String checkTicketBeforeVerify(TicketSerialVO ticket) {

        if (ticket == null) {
            return "查無此票券資料";
        }

        if (ticket.getStatus() == 3) {
            return "此票券已使用，無法重複驗票";
        }

        if (ticket.getStatus() == 4) {
            return "此票券已過期，無法驗票";
        }

        if (ticket.getStatus() == 6) {
            return "此票券已退款或作廢，無法驗票";
        }

        if (ticket.getStatus() != 2) {
            return "此票券目前無法驗票";
        }

        if (ticket.getExpiryDate() == null) {
            return "此票券尚未設定可使用日期";
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate =
                ticket.getExpiryDate().toLocalDateTime();

        LocalDate useDate = expiryDate.toLocalDate();
        LocalDateTime startTime = useDate.atStartOfDay();

        if (now.isBefore(startTime)) {
            return "票券尚未到可使用日期，無法驗票";
        }

        if (now.isAfter(expiryDate)) {
            ticket.setStatus(4);
            myTicketRepository.save(ticket);
            return "票券已超過可使用日期，無法驗票";
        }

        return "OK";
    }

    @Transactional
    public String confirmVerifyTicket(String serialNumber) {

        TicketSerialVO ticket =
                myTicketRepository.findBySerialNumber(serialNumber).orElse(null);

        String checkResult = checkTicketBeforeVerify(ticket);

        if (!"OK".equals(checkResult)) {
            return checkResult;
        }

        ticket.setStatus(3);
        myTicketRepository.save(ticket);

        return "驗票成功";
    }
}