package com.taipeigo.ticketcategory.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TicketCategoryService {
	
	@Autowired
	private TicketCategoryRepository repository;
	

	public void addTicketCategory(TicketCategoryVO ticketCateVO) {
		repository.save(ticketCateVO);
	}
	
	
	public void updateTicketCategory(TicketCategoryVO ticketCateVO) {
		repository.save(ticketCateVO);
		// 後續優化：加上安全檢查確認前端傳來的欄位資料都在
	}
	

	public void deleteTicketCategory(Integer ticketCateId) {
		if (repository.existsById(ticketCateId)) {
			repository.deleteById(ticketCateId);
		}
	}
	
	
	public List<TicketCategoryVO> getAll() {
		return repository.findAll();
	}
	
	
	public TicketCategoryVO getOneTicketCategory(Integer ticketCateId) {
		Optional<TicketCategoryVO> optional = repository.findById(ticketCateId);
		return optional.orElse(null);
	}
	
	// 找出所有啟用狀態的 category
	public List<TicketCategoryVO> getAllActive() {
	    // 1 代表啟用
	    return repository.findByTicketCategoryStatus(1); 
	}
	
}

