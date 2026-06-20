package com.taipeigo.ticket.model;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;



@Service
public class TicketService {

	@Autowired
    private TicketRepository ticketRepository;
	
	@Autowired
	private TicketSerialRepository ticketSerialRepository;
	
	
	
	public List<TicketVO> getAll() {
		return ticketRepository.findAll();
	}
	
	public TicketVO getOneTicket(Integer ticketId) {
		Optional<TicketVO> optional=ticketRepository.findById(ticketId);
        return optional.orElse(null);
    }
	
	/* 新增門票序號 */
	@Transactional
    public void generateSerials(Integer ticketId, int quantity) {
        TicketVO ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("找不到該門票商品，編號: " + ticketId));

        for (int i = 0; i < quantity; i++) {
            TicketSerialVO serial = createNewAvailableSerial(ticket);
            ticketSerialRepository.save(serial);
        }
    }

    /**
     * 新增門票序號物件
     */
    private TicketSerialVO createNewAvailableSerial(TicketVO ticket) {
        TicketSerialVO serial = new TicketSerialVO();
        serial.setTicketVO(ticket);
        String uniqueCode = generateUniqueSerialNumber();
        serial.setSerialNumber(uniqueCode);
        serial.setCustomerVO(null);  
        serial.setStatus(0); 
        // 購買此門票序號時才會將使用日期代入，故用null
        serial.setExpiryDate(null);
        return serial;
    }

	
	/* 產生不重複門票序號，TK+9碼(如TKABC123XYZ) */
	public String generateUniqueSerialNumber() {
	    String prefix = "TK";
	    String serialNumber = "";
	    boolean isDuplicate = true;

	    while (isDuplicate) {
	        // 用 UUID 先排除 "-" 並設定大寫後再取9碼
	        String uuidStr = UUID.randomUUID().toString();
	        String cleanCode = uuidStr.replace("-", "").toUpperCase();
	        String randomCode = cleanCode.substring(0, 9);
	        
	        serialNumber = prefix + randomCode; 
	        // 去資料庫驗證有沒有人用了
	        isDuplicate = ticketSerialRepository.existsBySerialNumber(serialNumber);
	    }
	    return serialNumber;
	}
	
	
	
}
