package com.taipeigo.ticketcategory.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketCategoryService {

	@Autowired
	private TicketCategoryRepository repository;

	// 找出所有未被刪除的 category (分頁)
	public Page<TicketCategoryVO> getNotDeletedWithPage(int pageNumber) {
		Pageable pageable = PageRequest.of(pageNumber, 10); // 一頁 10 筆
		return repository.findAllNotDeleted(pageable);
	}

	// 模糊搜尋未被刪除的 category (分頁)
	public Page<TicketCategoryVO> searchByName(String keyword, int pageNumber) {
		Pageable pageable = PageRequest.of(pageNumber, 10); // 一頁 10 筆
		return repository.findByName(keyword, pageable);
	}

	public void addTicketCategory(TicketCategoryVO ticketCateVO) {
		repository.save(ticketCateVO);
	}

	public void updateTicketCategory(TicketCategoryVO ticketCateVO) {
		repository.save(ticketCateVO);
		// 後續優化：加上安全檢查確認前端傳來的欄位資料都在
	}

	@Transactional
	public void deleteTicketCategory(Integer ticketCateId) {
		Optional<TicketCategoryVO> optional = repository.findById(ticketCateId);
		if (optional.isPresent()) {
			TicketCategoryVO vo = optional.get();
			vo.setTicketCategoryStatus(2); // 代表軟刪除 => db保留只是狀態改變
			repository.save(vo);

			// 使用軟刪除，保留 TICKET_CATEGORY_INFO 資料庫中的關聯 => 但畫面不顯示已刪除的分類
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

	// 找出所有未被刪除的 category => 啟用 和 未啟用
	public List<TicketCategoryVO> getAllNotDeleted() {
		return repository.findAllNotDeleted();
	}

}
