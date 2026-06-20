package com.taipeigo.ticket.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class TicketService {

	@Autowired
    private TicketRepository ticketRepository;
	
	
	
	public List<TicketVO> getAll() {
		return ticketRepository.findAll();
	}
	
	public TicketVO getOneTicket(Integer ticketId) {
		Optional<TicketVO> optional=ticketRepository.findById(ticketId);
        return optional.orElse(null);
    }
	
	
	
}
